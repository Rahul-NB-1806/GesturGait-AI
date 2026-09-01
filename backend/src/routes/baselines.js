const express = require('express');
const FeatureWindow = require('../models/FeatureWindow');
const Baseline = require('../models/Baseline');
const User = require('../models/User');
const { authenticate } = require('../middleware/auth');

const router = express.Router();

const FEATURE_KEYS = [
  'stepCount', 'avgStepTime', 'peakFrequency',
  'tremorFrequency', 'movementStability',
];

router.get('/:userId', authenticate, async (req, res) => {
  try {
    const userId = req.params.userId;
    const baseline = await Baseline.findOne({ userId });
    if (baseline) {
      return res.json(baseline);
    }

    const user = await User.findOne({ patientId: userId });
    if (!user) {
      // Try by MongoDB ID as fallback
      const userById = await User.findById(userId).catch(() => null);
      if (!userById) return res.status(404).json({ message: 'User not found' });
    }

    const firstWindow = await FeatureWindow.findOne({ userId })
      .sort({ timestamp: 1 });
    if (!firstWindow) {
      return res.json({
        daysCollected: 0,
        daysRequired: user.baselineWindowDays,
        establishedAt: null,
      });
    }

    const daysElapsed = Math.ceil(
      (Date.now() - new Date(firstWindow.timestamp).getTime()) / (1000 * 60 * 60 * 24)
    );
    const daysCollected = Math.min(daysElapsed, user.baselineWindowDays);

    return res.json({
      daysCollected,
      daysRequired: user.baselineWindowDays,
      establishedAt: null,
    });
  } catch (err) {
    res.status(500).json({ message: 'Failed to get baseline' });
  }
});

router.post('/:userId/recalculate', authenticate, async (req, res) => {
  try {
    const windows = await FeatureWindow.find({ userId: req.params.userId })
      .sort({ timestamp: -1 })
      .limit(1000);

    if (windows.length === 0) {
      return res.status(400).json({ message: 'No feature data to compute baseline' });
    }

    const averages = {};
    const stdDevs = {};

    FEATURE_KEYS.forEach((key) => {
      const values = windows.map((w) => w.features[key]).filter((v) => v != null);
      if (values.length === 0) {
        averages[key] = 0;
        stdDevs[key] = 0;
        return;
      }
      const mean = values.reduce((a, b) => a + b, 0) / values.length;
      const variance = values.reduce((sum, v) => sum + (v - mean) ** 2, 0) / values.length;
      averages[key] = Math.round(mean * 10000) / 10000;
      stdDevs[key] = Math.round(Math.sqrt(variance) * 10000) / 10000;
    });

    const baseline = await Baseline.findOneAndUpdate(
      { userId: req.params.userId },
      {
        userId: req.params.userId,
        establishedAt: new Date(),
        featureAverages: averages,
        featureStdDev: stdDevs,
        sampleWindowCount: windows.length,
      },
      { upsert: true, new: true }
    );

    await User.findOneAndUpdate(
      { patientId: req.params.userId },
      { baselineEstablished: true }
    );

    res.json(baseline);
  } catch (err) {
    res.status(500).json({ message: 'Failed to recalculate baseline' });
  }
});

module.exports = router;
