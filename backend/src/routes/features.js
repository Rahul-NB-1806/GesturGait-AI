const express = require('express');
const FeatureWindow = require('../models/FeatureWindow');
const { authenticate } = require('../middleware/auth');

const router = express.Router();

router.post('/', authenticate, async (req, res) => {
  try {
    const { windows } = req.body;
    if (!windows || !Array.isArray(windows)) {
      return res.status(400).json({ message: 'Array of feature windows required' });
    }

    const docs = windows.map((w) => ({
      userId: req.user._id,
      timestamp: w.timestamp || new Date(),
      windowDurationSec: w.windowDurationSec || 5,
      features: w.features,
      source: w.source || 'sensor',
    }));

    const saved = await FeatureWindow.insertMany(docs);
    res.status(201).json({ count: saved.length });
  } catch (err) {
    res.status(500).json({ message: 'Failed to save features' });
  }
});

module.exports = router;
