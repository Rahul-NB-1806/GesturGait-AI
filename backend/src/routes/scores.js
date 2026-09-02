const express = require('express');
const FeatureWindow = require('../models/featurewindow');
const Baseline = require('../models/baseline');
const RiskScore = require('../models/riskscore');
const { authenticate } = require('../middleware/auth');

const router = express.Router();

const FEATURE_KEYS = [
  'stepCount', 'avgStepTime', 'peakFrequency',
  'tremorFrequency', 'movementStability',
  'swipeSpeed', 'tapInterval', 'gestureDurationVariance'
];

// Sync daily score from mobile app
router.post('/sync', async (req, res) => {
  try {
    const { userId, date, score, confidence, stepCount, features, deviations, explanation, recommendation } = req.body;

    const riskScore = await RiskScore.findOneAndUpdate(
      { userId, date },
      {
        score,
        confidence,
        stepCount,
        features,
        deviations,
        explanation,
        recommendation
      },
      { upsert: true, new: true }
    );

    res.json({ success: true, data: riskScore });
  } catch (err) {
    console.error('Sync error:', err);
    res.status(500).json({ message: 'Failed to sync score' });
  }
});

router.get('/:userId/history', async (req, res) => {
  try {
    const history = await RiskScore.find({ userId: req.params.userId })
      .sort({ date: -1 })
      .limit(90);
    res.json({ history });
  } catch (err) {
    res.status(500).json({ message: 'Failed to get history' });
  }
});

router.get('/:userId/latest', async (req, res) => {
  try {
    const latest = await RiskScore.findOne({ userId: req.params.userId })
      .sort({ date: -1 });
    res.json(latest);
  } catch (err) {
    res.status(500).json({ message: 'Failed to get latest score' });
  }
});

router.get('/:userId/summary', authenticate, async (req, res) => {
  try {
    const { period } = req.query;
    if (!['weekly', 'monthly'].includes(period)) {
      return res.status(400).json({ message: 'period must be "weekly" or "monthly"' });
    }

    const scores = await RiskScore.find({ userId: req.params.userId })
      .sort({ date: -1 })
      .lean();

    if (scores.length === 0) {
      return res.json({ period, data: [] });
    }

    const groups = {};

    scores.forEach((s) => {
      const d = new Date(s.date);
      let key;
      if (period === 'weekly') {
        const startOfYear = new Date(d.getFullYear(), 0, 1);
        const diff = d - startOfYear;
        const oneWeek = 604800000;
        const weekNum = Math.ceil((diff / oneWeek + startOfYear.getDay() + 1) / 7);
        key = `${d.getFullYear()}-W${String(weekNum).padStart(2, '0')}`;
      } else {
        key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
      }

      if (!groups[key]) {
        groups[key] = { scores: [], min: Infinity, max: -Infinity, sum: 0 };
      }
      groups[key].scores.push(s);
      groups[key].min = Math.min(groups[key].min, s.score);
      groups[key].max = Math.max(groups[key].max, s.score);
      groups[key].sum += s.score;
    });

    const sortedKeys = Object.keys(groups).sort().reverse();
    const limit = period === 'weekly' ? 4 : 6;
    const topKeys = sortedKeys.slice(0, limit);

    const data = topKeys.map((key) => {
      const g = groups[key];
      return {
        label: key,
        avgScore: Math.round(g.sum / g.scores.length),
        minScore: g.min,
        maxScore: g.max,
        scoreCount: g.scores.length,
      };
    });

    res.json({ period, data });
  } catch (err) {
    res.status(500).json({ message: 'Failed to get summary' });
  }
});

function fallbackScore(baseline, todayFeatures) {
  const deviations = [];
  let totalZ = 0;
  let count = 0;

  FEATURE_KEYS.forEach((key) => {
    const value = todayFeatures[key];
    const mean = baseline.featureAverages[key];
    const std = baseline.featureStdDev[key];
    if (value != null && mean != null && std != null && std > 0) {
      const z = (value - mean) / std;
      totalZ += Math.abs(z);
      count++;

      const deltaPercent = mean !== 0 ? ((value - mean) / mean) * 100 : 0;
      deviations.push({
        feature: key,
        deltaPercent: Math.round(deltaPercent * 10) / 10,
        direction: deltaPercent > 0 ? 'better' : 'worse',
      });
    }
  });

  const avgZ = count > 0 ? totalZ / count : 0;
  const score = Math.min(100, Math.max(0, Math.round(avgZ * 20)));

  const significantDeviations = deviations.filter((d) => Math.abs(d.deltaPercent) > 10);
  const worstDev = significantDeviations[0];

  let explanation = significantDeviations.length > 0
    ? `${significantDeviations.slice(0, 3).map((d) => `${d.feature} ${d.direction === 'worse' ? 'down' : 'up'} ${Math.abs(d.deltaPercent).toFixed(0)}%`).join(', ')} from your baseline`
    : 'All metrics are within normal range compared to your baseline';

  let recommendation;
  if (score < 30) {
    recommendation = 'No immediate action needed. Continue monitoring as usual.';
  } else if (score < 60) {
    recommendation = 'Minor deviations detected. Monitor for trends over the coming days.';
  } else {
    recommendation = 'Neurological risk: moderate. Consider consulting a neurologist to discuss these changes.';
  }

  return { score, deviations, explanation, recommendation };
}

module.exports = router;
