const BASELINE_DAYS = 7;
const SAMPLES_PER_DAY = 288;
const MIN_BASELINE_SAMPLES = 50;

const EMA_ALPHA = 0.1;

class AdaptiveBaseline {
  constructor() {
    this.mean = null;
    this.std = null;
    this.ema = null;
    this.sampleCount = 0;
    this.dayCount = 0;
    this.lastUpdateDay = null;
    this.featureHistory = [];
  }

  static fromStorage(data) {
    const baseline = new AdaptiveBaseline();
    if (data) {
      baseline.mean = data.mean;
      baseline.std = data.std;
      baseline.ema = data.ema;
      baseline.sampleCount = data.sampleCount || 0;
      baseline.dayCount = data.dayCount || 0;
      baseline.lastUpdateDay = data.lastUpdateDay;
      baseline.featureHistory = data.featureHistory || [];
    }
    return baseline;
  }

  toStorage() {
    return {
      mean: this.mean,
      std: this.std,
      ema: this.ema,
      sampleCount: this.sampleCount,
      dayCount: this.dayCount,
      lastUpdateDay: this.lastUpdateDay,
      featureHistory: this.featureHistory.slice(-SAMPLES_PER_DAY * BASELINE_DAYS),
    };
  }

  get isLearningPhase() {
    return this.dayCount < BASELINE_DAYS;
  }

  get progressPercent() {
    return Math.min(100, Math.round((this.sampleCount / (MIN_BASELINE_SAMPLES * BASELINE_DAYS)) * 100));
  }

  update(features) {
    const today = new Date().toDateString();
    
    if (this.lastUpdateDay !== today) {
      this.dayCount = Math.min(this.dayCount + 1, BASELINE_DAYS);
      this.lastUpdateDay = today;
    }

    this.featureHistory.push({
      ...features,
      timestamp: Date.now(),
      day: this.dayCount,
    });

    if (this.featureHistory.length > SAMPLES_PER_DAY * BASELINE_DAYS) {
      this.featureHistory.shift();
    }

    this.sampleCount++;

    if (this.mean === null) {
      this.mean = { ...features };
      this.std = Object.keys(features).reduce((acc, k) => ({ ...acc, [k]: 0 }), {});
      this.ema = { ...features };
    } else {
      const keys = Object.keys(features);
      
      keys.forEach(key => {
        const prevMean = this.mean[key];
        const prevStd = this.std[key];
        const value = features[key];

        this.mean[key] = prevMean + EMA_ALPHA * (value - prevMean);
        this.std[key] = Math.sqrt(
          (1 - EMA_ALPHA) * (prevStd ** 2 + EMA_ALPHA * (value - prevMean) ** 2)
        );
        this.ema[key] = this.ema[key] + EMA_ALPHA * (value - this.ema[key]);
      });
    }

    return this.getStatus();
  }

  getStatus() {
    return {
      isLearningPhase: this.isLearningPhase,
      dayCount: this.dayCount,
      totalDays: BASELINE_DAYS,
      sampleCount: this.sampleCount,
      progressPercent: this.progressPercent,
      mean: this.mean,
      std: this.std,
      ema: this.ema,
    };
  }

  getDeviation(features) {
    if (this.mean === null) {
      return null;
    }

    const keys = Object.keys(features);
    const deviations = {};
    
    keys.forEach(key => {
      const std = this.std[key] || 1;
      const mean = this.mean[key] || 0;
      deviations[key] = std > 0 ? (features[key] - mean) / std : 0;
    });

    const magnitude = Math.sqrt(
      Object.values(deviations).reduce((sum, d) => sum + d * d, 0)
    );

    return {
      deviations,
      magnitude: Math.round(magnitude * 100) / 100,
      isAnomalous: magnitude > 2,
    };
  }

  canScore() {
    return !this.isLearningPhase && this.sampleCount >= MIN_BASELINE_SAMPLES;
  }
}

export { AdaptiveBaseline, BASELINE_DAYS, MIN_BASELINE_SAMPLES };