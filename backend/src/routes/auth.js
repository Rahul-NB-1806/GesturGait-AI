const express = require('express');
const User = require('../models/user');
const { generateToken } = require('../middleware/auth');

const router = express.Router();

router.post('/register', async (req, res) => {
  try {
    const { email, password, deviceId } = req.body;
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

    // Generate Patient ID based on Device ID if provided, otherwise random
    let patientId;
    if (deviceId && deviceId !== 'unknown') {
      // Create a 6-digit numeric ID from the hash of the device ID
      const crypto = require('crypto');
      const hash = crypto.createHash('md5').update(deviceId).digest('hex');
      const numericPart = parseInt(hash.substring(0, 8), 16) % 900000 + 100000;
      patientId = `GG-${numericPart}`;
    } else {
      patientId = `GG-${Math.floor(100000 + Math.random() * 900000)}`;
    }

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
    console.error('Registration error:', err);
    res.status(500).json({ message: 'Registration failed', error: err.message });
  }
});

router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;
    console.log(`Login attempt for: ${email}`);

    if (!email || !password) {
      return res.status(400).json({ message: 'Email and password required' });
    }

    const user = await User.findOne({ email: email.toLowerCase() });
    if (!user) {
      console.log(`Login failed: User ${email} not found`);
      return res.status(401).json({ message: 'Invalid email or password' });
    }

    const isValid = await user.comparePassword(password);
    if (!isValid) {
      console.log(`Login failed: Invalid password for ${email}`);
      return res.status(401).json({ message: 'Invalid email or password' });
    }

    console.log(`Login success: ${email} (${user.patientId})`);
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
