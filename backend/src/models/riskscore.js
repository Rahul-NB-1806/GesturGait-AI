const mongoose = require('mongoose');

const deviationSchema = new mongoose.Schema({
  feature: String,
  deltaPercent: Number,
  direction: String,
}, { _id: false });

const riskScoreSchema = new mongoose.Schema({
  userId: {
    type: String,
    required: true,
    index: true,
  },
  date: {
    type: String, // Stored as YYYY-MM-DD
    required: true,
  },
  score: {
    type: Number,
    min: 0,
    max: 100,
    required: true,
  },
  confidence: {
    type: Number,
    min: 0,
    max: 100,
  },
  stepCount: Number,
  features: {
    type: Map,
    of: Number,
  },
  deviations: [deviationSchema],
  explanation: String,
  recommendation: String,
}, { timestamps: true });

riskScoreSchema.index({ userId: 1, date: -1 }, { unique: true });

module.exports = mongoose.model('RiskScore', riskScoreSchema);
