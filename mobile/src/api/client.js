import AsyncStorage from '@react-native-async-storage/async-storage';

const API_BASE = 'http://10.0.2.2:3000';

async function getToken() {
  return await AsyncStorage.getItem('token');
}

async function request(endpoint, options = {}) {
  const token = await getToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const res = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
  });

  const data = await res.json();
  if (!res.ok) throw new Error(data.message || 'Request failed');
  return data;
}

async function register(email, password) {
  return request('/auth/register', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });
}

async function login(email, password) {
  return request('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });
}

async function uploadFeatures(features) {
  return request('/features', {
    method: 'POST',
    body: JSON.stringify({ windows: features }),
  });
}

async function getBaseline(userId) {
  return request(`/baseline/${userId}`);
}

async function recalculateBaseline(userId) {
  return request(`/baseline/${userId}/recalculate`, { method: 'POST' });
}

async function getTodayScore(userId) {
  return request(`/score/${userId}/today`);
}

async function getScoreHistory(userId) {
  return request(`/score/${userId}/history`);
}

async function getScoreSummary(userId, period) {
  return request(`/score/${userId}/summary?period=${period}`);
}

export {
  register,
  login,
  uploadFeatures,
  getBaseline,
  recalculateBaseline,
  getTodayScore,
  getScoreHistory,
  getScoreSummary,
};
