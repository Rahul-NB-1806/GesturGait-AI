import { Accelerometer, Gyroscope } from 'expo-sensors';
import { RingBuffer, SAMPLE_RATE } from './RingBuffer';

Accelerometer.setUpdateInterval(1000 / SAMPLE_RATE);
Gyroscope.setUpdateInterval(1000 / SAMPLE_RATE);

class SensorManager {
  constructor() {
    this.accelBuffer = new RingBuffer();
    this.gyroBuffer = new RingBuffer();
    this._accelSub = null;
    this._gyroSub = null;
    this._isMonitoring = false;
    this._onWindowReady = null;
    this._lastAccelTime = null;
    this._accelSamples = [];
  }

  set onWindowReady(callback) {
    this._onWindowReady = callback;
  }

  get isMonitoring() {
    return this._isMonitoring;
  }

  start() {
    if (this._isMonitoring) return;
    this._isMonitoring = true;
    this._accelSamples = [];

    this._accelSub = Accelerometer.addListener((data) => {
      const now = Date.now();
      const sample = { ...data, timestamp: now };
      this.accelBuffer.push(sample);
      this._accelSamples.push(sample);

      if (this.accelBuffer.isFull) {
        const window = this.accelBuffer.toArray();
        this.accelBuffer.clear();
        if (this._onWindowReady) {
          this._onWindowReady(window);
        }
      }
    });

    this._gyroSub = Gyroscope.addListener((data) => {
      const sample = { ...data, timestamp: Date.now() };
      this.gyroBuffer.push(sample);
    });
  }

  stop() {
    if (!this._isMonitoring) return;
    this._isMonitoring = false;

    if (this._accelSub) {
      this._accelSub.remove();
      this._accelSub = null;
    }
    if (this._gyroSub) {
      this._gyroSub.remove();
      this._gyroSub = null;
    }
    this.accelBuffer.clear();
    this.gyroBuffer.clear();
    this._accelSamples = [];
  }

  destroy() {
    this.stop();
    this._onWindowReady = null;
  }
}

export default new SensorManager();
