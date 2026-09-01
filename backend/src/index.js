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
const PORT = process.env.PORT || 5000;
const MONGODB_URI = process.env.MONGODB_URI;

app.use(helmet());
app.use(cors({
  origin: '*',
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization']
}));
app.use(express.json({ limit: '10mb' }));

app.use((req, res, next) => {
  console.log(`${req.method} ${req.url}`);
  next();
});

app.use('/auth', authRoutes);
app.use('/features', featureRoutes);
app.use('/baseline', baselineRoutes);
app.use('/score', scoreRoutes);

app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    service: 'gesturgait-backend',
    database: mongoose.connection.readyState === 1 ? 'connected' : 'disconnected'
  });
});

if (!MONGODB_URI) {
  console.error("CRITICAL: MONGODB_URI is not defined in environment variables!");
  process.exit(1);
}

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
