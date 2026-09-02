const mongoose = require('mongoose');

const baselineSchema = new mongoose.Schema({
  userId: {
    type: String,
    required: true,
    unique: true,
  },
  establishedAt: {
    type: Date,
    default: Date.now,
  },
  featureAverages: {
    stepCount: { type: Number, default: 0 },
    avgStepTime: { type: Number, default: 0 },
    peakFrequency: { type: Number, default: 0 },
    tremorFrequency: { type: Number, default: 0 },
    movementStability: { type: Number, default: 0 },
    swipeSpeed: { type: Number },
    tapInterval: { type: Number },
    typingConsistency: { type: Number },
  },
  featureStdDev: {
    stepCount: { type: Number, default: 0 },
    avgStepTime: { type: Number, default: 0 },
    peakFrequency: { type: Number, default: 0 },
    tremorFrequency: { type: Number, default: 0 },
    movementStability: { type: Number, default: 0 },
    swipeSpeed: { type: Number },
    tapInterval: { type: Number },
    typingConsistency: { type: Number },
  },
  sampleWindowCount: {
    type: Number,
    default: 0,
  },
}, { timestamps: true });

module.exports = mongoose.model('Baseline', baselineSchema);
