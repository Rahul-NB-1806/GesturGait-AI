import { dominantFrequency, bandPower } from '../utils/fft';

function detectPeaks(values, minDistance = 10) {
  const peaks = [];
  for (let i = 1; i < values.length - 1; i++) {
    if (values[i] > values[i - 1] && values[i] > values[i + 1]) {
      if (peaks.length === 0 || (i - peaks[peaks.length - 1].index) >= minDistance) {
        peaks.push({ index: i, value: values[i] });
      }
    }
  }
  return peaks;
}

function extractFeatures(accelWindow) {
  const timestamps = accelWindow.map(s => s.timestamp);
  const x = accelWindow.map(s => s.x);
  const y = accelWindow.map(s => s.y);
  const z = accelWindow.map(s => s.z);

  const magnitude = accelWindow.map(s =>
    Math.sqrt(s.x * s.x + s.y * s.y + s.z * s.z)
  );

  const verticalAccel = z;

  const peaks = detectPeaks(verticalAccel);
  const stepCount = peaks.length;

  let avgStepTime = 0;
  if (stepCount > 1) {
    const stepTimes = [];
    for (let i = 1; i < peaks.length; i++) {
      stepTimes.push(
        (timestamps[peaks[i].index] - timestamps[peaks[i - 1].index]) / 1000
      );
    }
    avgStepTime =
      stepTimes.reduce((a, b) => a + b, 0) / stepTimes.length;
  }

  const peakFreq = dominantFrequency(magnitude, 50);
  const tremorFreq = bandPower(magnitude, 4, 6, 50);

  const meanMag = magnitude.reduce((a, b) => a + b, 0) / magnitude.length;
  const variance =
    magnitude.reduce((sum, v) => sum + (v - meanMag) ** 2, 0) / magnitude.length;
  const movementStability = Math.sqrt(variance);

  return {
    stepCount: Math.round(stepCount),
    avgStepTime: Math.round(avgStepTime * 1000) / 1000,
    peakFrequency: Math.round(peakFreq * 100) / 100,
    tremorFrequency: Math.round(tremorFreq * 100) / 100,
    movementStability: Math.round(movementStability * 10000) / 10000,
  };
}

export { extractFeatures };
