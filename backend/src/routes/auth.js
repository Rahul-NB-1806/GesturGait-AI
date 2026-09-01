const express = require('express');
const User = require('../models/User');
const { generateToken } = require('../middleware/auth');

const router = express.Router();

router.post('/register', async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) {
      return res.status(400).json({ message: 'Email and password required' });
    }
    if (password.length < 6) {
      return res.status(400).json({ message: 'Password must be at least 6 characters' });
    }

    const existingUser = await User.findOne({ email: email.toLowerCase() });
    if (existingUser) {
      return res.status(409).json({ message: 'Email already registered' });
    }

    const patientId = `GG-${Math.floor(100000 + Math.random() * 900000)}`;
    const user = new User({ email, passwordHash: password, patientId });
    await user.save();

    const token = generateToken(user);
    res.status(201).json({
      token,
      user: {
        _id: user._id,
        email: user.email,
        patientId: user.patientId,
        baselineEstablished: user.baselineEstablished,
        baselineWindowDays: user.baselineWindowDays,
      },
    });
  } catch (err) {
    res.status(500).json({ message: 'Registration failed' });
  }
});

router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) {
      return res.status(400).json({ message: 'Email and password required' });
    }

    const user = await User.findOne({ email: email.toLowerCase() });
    if (!user) {
      return res.status(401).json({ message: 'Invalid email or password' });
    }

    const isValid = await user.comparePassword(password);
    if (!isValid) {
      return res.status(401).json({ message: 'Invalid email or password' });
    }

    const token = generateToken(user);
    res.json({
      token,
      user: {
        _id: user._id,
        email: user.email,
        patientId: user.patientId,
        baselineEstablished: user.baselineEstablished,
        baselineWindowDays: user.baselineWindowDays,
      },
    });
  } catch (err) {
    res.status(500).json({ message: 'Login failed' });
  }
});

module.exports = router;
