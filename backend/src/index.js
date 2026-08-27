const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const helmet = require('helmet');
require('dotenv').config();

const authRoutes = require('./routes/auth');
const featureRoutes = require('./routes/features');
const baselineRoutes = require('./routes/baselines');
const scoreRoutes = require('./routes/scores');

const app = express();
const PORT = process.env.PORT || 3000;
const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/gesturgait';

app.use(helmet());
app.use(cors());
app.use(express.json({ limit: '1mb' }));

app.use('/auth', authRoutes);
app.use('/features', featureRoutes);
app.use('/baseline', baselineRoutes);
app.use('/score', scoreRoutes);

app.get('/health', (req, res) => {
  res.json({ status: 'ok', service: 'gesturgait-backend' });
});

mongoose.connect(MONGODB_URI)
  .then(() => {
    console.log('Connected to MongoDB');
    app.listen(PORT, () => {
      console.log(`GesturGait API running on port ${PORT}`);
    });
  })
  .catch((err) => {
    console.error('MongoDB connection error:', err.message);
    console.log('Starting server without database connection...');
    app.listen(PORT, () => {
      console.log(`GesturGait API running on port ${PORT} (no DB)`);
    });
  });

module.exports = app;
