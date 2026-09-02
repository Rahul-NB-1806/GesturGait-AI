const mongoose = require('mongoose');
const RiskScore = require('./models/riskscore');
require('dotenv').config();

const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/gesturgait';

const patientId = 'GG-8F42K91';

const seedData = async () => {
  try {
    await mongoose.connect(MONGODB_URI);
    console.log('Connected to MongoDB for seeding...');

    await RiskScore.deleteMany({ userId: patientId });

    const history = [];
    const now = new Date();

    for (let i = 0; i < 30; i++) {
      const date = new Date();
      date.setDate(now.getDate() - i);
      const dateStr = date.toISOString().split('T')[0];

      history.push({
        userId: patientId,
        date: dateStr,
        score: Math.floor(40 + Math.random() * 30),
        confidence: 90 + Math.random() * 5,
        stepCount: Math.floor(5000 + Math.random() * 3000),
        explanation: 'Stable activity patterns observed.',
        recommendation: 'Continue regular monitoring.',
        deviations: [
          { feature: 'swipeSpeed', deltaPercent: Math.random() * 5, direction: 'better' },
          { feature: 'tremorFrequency', deltaPercent: Math.random() * 5, direction: 'worse' }
        ]
      });
    }

    await RiskScore.insertMany(history);
    console.log('Seed data inserted successfully for', patientId);
    process.exit(0);
  } catch (err) {
    console.error('Seed error:', err);
    process.exit(1);
  }
};

seedData();
