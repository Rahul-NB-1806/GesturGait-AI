const mongoose = require('mongoose');

const featureWindowSchema = new mongoose.Schema({
  userId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true,
    index: true,
  },
  timestamp: {
    type: Date,
    required: true,
  },
  windowDurationSec: {
    type: Number,
    default: 5,
  },
  features: {
    stepCount: { type: Number, default: 0 },
    avgStepTime: { type: Number, default: 0 },
    peakFrequency: { type: Number, default: 0 },
    tremorFrequency: { type: Number, default: 0 },
    movementStability: { type: Number, default: 0 },
    swipeSpeed: { type: Number },
    tapInterval: { type: Number },
    typingConsistency: { type: Number },
  },
  source: {
    type: String,
    enum: ['sensor', 'keyboard', 'accessibility'],
    default: 'sensor',
  },
}, { timestamps: true });

featureWindowSchema.index({ userId: 1, timestamp: -1 });

module.exports = mongoose.model('FeatureWindow', featureWindowSchema);
