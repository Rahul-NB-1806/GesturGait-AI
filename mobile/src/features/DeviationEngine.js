const FEATURE_WEIGHTS = {
  stepCount: 1.0,
  avgStepTime: 1.5,
  peakFrequency: 1.0,
  tremorFrequency: 2.0,
  movementStability: 1.5,
};

const DEVIATION_THRESHOLDS = {
  mild: 1.0,
  moderate: 2.0,
  severe: 3.0,
};

function computeWeightedDeviation(deviations, weights = FEATURE_WEIGHTS) {
  let weightedSum = 0;
  let weightTotal = 0;

  Object.entries(deviations).forEach(([key, value]) => {
    const weight = weights[key] || 1.0;
    weightedSum += Math.abs(value) * weight;
    weightTotal += weight;
  });

  return weightTotal > 0 ? weightedSum / weightTotal : 0;
}

function classifyDeviation(magnitude) {
  if (magnitude >= DEVIATION_THRESHOLDS.severe) return 'severe';
  if (magnitude >= DEVIATION_THRESHOLDS.moderate) return 'moderate';
  if (magnitude >= DEVIATION_THRESHOLDS.mild) return 'mild';
  return 'normal';
}

function generateExplanation(deviations, features) {
  const explanations = [];
  const featureLabels = {
    stepCount: 'Step count',
    avgStepTime: 'Step timing',
    peakFrequency: 'Walking rhythm',
    tremorFrequency: 'Tremor (4-6Hz)',
    movementStability: 'Movement stability',
  };

  Object.entries(deviations).forEach(([key, value]) => {
    if (Math.abs(value) > 1.0) {
      const direction = value > 0 ? 'increased' : 'decreased';
      const label = featureLabels[key] || key;
      explanations.push(`${label} ${direction} (${value > 0 ? '+' : ''}${value.toFixed(1)}σ)`);
    }
  });

  if (explanations.length === 0) {
    explanations.push('All features within normal range');
  }

  return explanations;
}

export function analyzeDeviation(baseline, features) {
  if (!baseline || !baseline.mean) {
    return {
      deviation: null,
      magnitude: 0,
      classification: 'learning',
      explanation: ['Building baseline...'],
      canScore: false,
    };
  }

  const baselineDeviation = baseline.getDeviation(features);
  
  if (!baselineDeviation) {
    return {
      deviation: null,
      magnitude: 0,
      classification: 'learning',
      explanation: ['Building baseline...'],
      canScore: false,
    };
  }

  const weightedMagnitude = computeWeightedDeviation(baselineDeviation.deviations);
  const classification = classifyDeviation(weightedMagnitude);
  const explanation = generateExplanation(baselineDeviation.deviations, features);
  const canScore = baseline.canScore();

  return {
    deviation: baselineDeviation.deviations,
    magnitude: Math.round(weightedMagnitude * 100) / 100,
    rawMagnitude: baselineDeviation.magnitude,
    classification,
    isAnomalous: baselineDeviation.isAnomalous,
    explanation,
    canScore,
    baselineStatus: baseline.getStatus(),
  };
}

export { FEATURE_WEIGHTS, DEVIATION_THRESHOLDS };