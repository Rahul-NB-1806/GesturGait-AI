const jwt = require('jsonwebtoken');
const User = require('../models/User');

const JWT_SECRET = process.env.JWT_SECRET;

if (!JWT_SECRET && process.env.NODE_ENV === 'production') {
  console.error("CRITICAL: JWT_SECRET is not defined in production environment variables!");
  process.exit(1);
}

// Fallback for local development only
const finalSecret = JWT_SECRET || 'gesturgait-dev-secret-change-in-production';

function generateToken(user) {
  // Removed 'expiresIn' so the user stays logged in indefinitely until manual logout
  return jwt.sign({ userId: user._id, email: user.email }, finalSecret);
}

async function authenticate(req, res, next) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ message: 'Authentication required' });
  }

  const token = authHeader.split(' ')[1];
  try {
    const decoded = jwt.verify(token, finalSecret);
    const user = await User.findById(decoded.userId);
    if (!user) {
      return res.status(401).json({ message: 'User not found' });
    }
    req.user = user;
    next();
  } catch (err) {
    return res.status(401).json({ message: 'Invalid or expired token' });
  }
}

module.exports = { generateToken, authenticate, JWT_SECRET };
