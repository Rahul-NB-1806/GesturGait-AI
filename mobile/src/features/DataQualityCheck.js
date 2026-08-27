const MIN_SAMPLES = 200;
const MAX_GAP_MS = 100;
const MIN_STEP_COUNT = 1;
const MAX_STEP_COUNT = 10;

export function checkDataQuality(window, features) {
  const issues = [];

  if (!window || window.length === 0) {
    issues.push('Empty window');
    return { valid: false, issues, qualityScore: 0 };
  }

  if (window.length < MIN_SAMPLES) {
    issues.push(`Insufficient samples: ${window.length}/${MIN_SAMPLES}`);
  }

  const timestamps = window.map(s => s.timestamp);
  const gaps = [];
  for (let i = 1; i < timestamps.length; i++) {
    const gap = timestamps[i] - timestamps[i - 1];
    if (gap > MAX_GAP_MS) {
      gaps.push(gap);
    }
  }
  if (gaps.length > timestamps.length * 0.1) {
    issues.push(`Excessive gaps: ${gaps.length} gaps > ${MAX_GAP_MS}ms`);
  }

  if (features.stepCount < MIN_STEP_COUNT || features.stepCount > MAX_STEP_COUNT) {
    issues.push(`Abnormal step count: ${features.stepCount}`);
  }

  if (features.movementStability < 0 || features.movementStability > 50) {
    issues.push(`Abnormal stability: ${features.movementStability}`);
  }

  const qualityScore = Math.max(0, 100 - issues.length * 25);

  return {
    valid: issues.length === 0,
    issues,
    qualityScore,
    sampleCount: window.length,
    gapCount: gaps.length,
  };
}

export function shouldScore(qualityResult) {
  return qualityResult.valid && qualityResult.qualityScore >= 50;
}