import React, { useState, useEffect, useRef } from 'react';
import {
  View, Text, StyleSheet, TouchableOpacity, Alert, ScrollView,
} from 'react-native';
import { Accelerometer } from 'expo-sensors';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../theme';
import sensorManager from '../sensors/SensorManager';
import { extractFeatures } from '../features/FeatureExtractor';
import { checkDataQuality, shouldScore } from '../features/DataQualityCheck';
import { AdaptiveBaseline } from '../features/AdaptiveBaseline';
import { analyzeDeviation } from '../features/DeviationEngine';
import { uploadFeatures } from '../api/client';
import AsyncStorage from '@react-native-async-storage/async-storage';

export default function MonitoringScreen() {
  const { isAuthenticated, user } = useAuth();
  const { colors, borderRadius: br, spacing } = useTheme();
  const [isMonitoring, setIsMonitoring] = useState(false);
  const [sensorStatus, setSensorStatus] = useState('idle');
  const [lastFeatures, setLastFeatures] = useState(null);
  const [featureLog, setFeatureLog] = useState([]);
  const [accelerometerData, setAccelerometerData] = useState({ x: 0, y: 0, z: 0 });
  const [hasSensor, setHasSensor] = useState(false);
  const [qualityResult, setQualityResult] = useState(null);
  const [deviationResult, setDeviationResult] = useState(null);
  const [baselineStatus, setBaselineStatus] = useState(null);
  const windowCountRef = useRef(0);
  const baselineRef = useRef(null);

  useEffect(() => {
    Accelerometer.isAvailableAsync().then(setHasSensor);
    loadBaseline();
  }, []);

  async function loadBaseline() {
    try {
      const stored = await AsyncStorage.getItem('adaptiveBaseline');
      if (stored) {
        const data = JSON.parse(stored);
        baselineRef.current = AdaptiveBaseline.fromStorage(data);
        setBaselineStatus(baselineRef.current.getStatus());
      } else {
        baselineRef.current = new AdaptiveBaseline();
        setBaselineStatus(baselineRef.current.getStatus());
      }
    } catch (e) {
      baselineRef.current = new AdaptiveBaseline();
      setBaselineStatus(baselineRef.current.getStatus());
    }
  }

  async function saveBaseline() {
    if (baselineRef.current) {
      try {
        await AsyncStorage.setItem('adaptiveBaseline', JSON.stringify(baselineRef.current.toStorage()));
      } catch (e) {
        console.warn('Failed to save baseline:', e);
      }
    }
  }

  function handleWindowReady(window) {
    console.log('[GesturGaitFeatures] Feature window received from native Android');
    const features = extractFeatures(window);
    console.log('[GesturGaitFeatures] status=RECEIVED featureCount=' + Object.keys(features).length);
    const quality = checkDataQuality(window, features);
    setQualityResult(quality);

    windowCountRef.current += 1;
    const entry = { ...features, window: windowCountRef.current };
    setLastFeatures(entry);
    setFeatureLog(prev => [entry, ...prev].slice(0, 50));

    if (baselineRef.current) {
      baselineRef.current.update(features);
      const status = baselineRef.current.getStatus();
      setBaselineStatus(status);

      const deviation = analyzeDeviation(baselineRef.current, features);
      setDeviationResult(deviation);

      saveBaseline();
    }

    if (isAuthenticated && user && shouldScore(quality)) {
      uploadFeatures([{
        userId: user._id,
        timestamp: new Date().toISOString(),
        windowDurationSec: 5,
        features,
        source: 'sensor',
        qualityScore: quality.qualityScore,
      }]).catch(() => {});
    }
  }

  function toggleMonitoring() {
    if (isMonitoring) {
      sensorManager.stop();
      setIsMonitoring(false);
      setSensorStatus('stopped');
    } else {
      if (!hasSensor) {
        Alert.alert('Sensor Not Available', 'Accelerometer is not available on this device.');
        return;
      }
      windowCountRef.current = 0;
      sensorManager.onWindowReady = handleWindowReady;
      sensorManager.start();
      setIsMonitoring(true);
      setSensorStatus('active');
    }
  }

  function getDeviationColor(classification, colors) {
    switch (classification) {
      case 'normal': return colors.success;
      case 'mild': return colors.warning;
      case 'moderate': return colors.danger;
      case 'severe': return colors.danger;
      default: return colors.muted;
    }
  }

  useEffect(() => {
    return () => {
      sensorManager.destroy();
    };
  }, []);

  useEffect(() => {
    if (isMonitoring) {
      const sub = Accelerometer.addListener(data => {
        setAccelerometerData(data);
      });
      return () => sub.remove();
    }
  }, [isMonitoring]);

  return (
    <ScrollView style={[styles.container, { backgroundColor: colors.background }]} contentContainerStyle={styles.content}>
      <Text style={[styles.title, { color: colors.text }]}>Monitoring</Text>

      <View style={[styles.statusCard, { backgroundColor: colors.card, borderRadius: br.md, marginBottom: spacing.md }]}>
        <View style={[styles.statusDot, isMonitoring ? styles.activeDot : styles.inactiveDot]} />
        <Text style={[styles.statusText, { color: colors.text }]}>
          {isMonitoring ? 'Monitoring Active' : 'Monitoring Stopped'}
        </Text>
      </View>

      <View style={[styles.sensorCard, { backgroundColor: colors.card, borderRadius: br.md, marginBottom: spacing.md }]}>
        <Text style={[styles.sensorLabel, { color: colors.muted }]}>Accelerometer</Text>
        <Text style={[styles.sensorValue, { color: colors.textSecondary }]}>
          X: {accelerometerData.x.toFixed(4)}{'\n'}
          Y: {accelerometerData.y.toFixed(4)}{'\n'}
          Z: {accelerometerData.z.toFixed(4)}
        </Text>
      </View>

      <TouchableOpacity
        style={[styles.button, isMonitoring ? styles.stopButton : styles.startButton]}
        onPress={toggleMonitoring}
      >
        <Text style={styles.buttonText}>
          {isMonitoring ? 'Stop Monitoring' : 'Start Monitoring'}
        </Text>
      </TouchableOpacity>

      {qualityResult && (
        <View style={[styles.qualityCard, { backgroundColor: colors.card, borderRadius: br.md, marginBottom: spacing.md }]}>
          <Text style={[styles.qualityTitle, { color: colors.muted }]}>Data Quality</Text>
          <Text style={[styles.qualityText, { color: qualityResult.valid ? colors.success : colors.warning }]}>
            {qualityResult.valid ? '✓ Valid' : '✗ Issues: ' + qualityResult.issues.join(', ')}
          </Text>
          <Text style={[styles.qualityDetail, { color: colors.textSecondary }]}>
            Samples: {qualityResult.sampleCount}/250 | Score: {qualityResult.qualityScore}%
          </Text>
        </View>
      )}

      {baselineStatus && (
        <View style={[styles.baselineCard, { backgroundColor: colors.card, borderRadius: br.md, marginBottom: spacing.md }]}>
          <Text style={[styles.baselineTitle, { color: colors.muted }]}>
            Baseline: {baselineStatus.isLearningPhase ? 'Learning' : 'Ready'}
          </Text>
          <Text style={[styles.baselineDetail, { color: colors.textSecondary }]}>
            Day {baselineStatus.dayCount}/{baselineStatus.totalDays} | {baselineStatus.progressPercent}% complete | {baselineStatus.sampleCount} samples
          </Text>
        </View>
      )}

      {deviationResult && deviationResult.canScore && (
        <View style={[styles.deviationCard, { backgroundColor: colors.card, borderRadius: br.md, marginBottom: spacing.md }]}>
          <Text style={[styles.deviationTitle, { color: colors.muted }]}>Deviation Analysis</Text>
          <Text style={[styles.deviationClass, { color: getDeviationColor(deviationResult.classification, colors) }]}>
            {deviationResult.classification.toUpperCase()} (magnitude: {deviationResult.magnitude})
          </Text>
          {deviationResult.explanation.map((exp, i) => (
            <Text key={i} style={[styles.explanationText, { color: colors.textSecondary }]}>
              • {exp}
            </Text>
          ))}
        </View>
      )}

      {lastFeatures && (
        <View style={[styles.featuresCard, { backgroundColor: colors.card, borderRadius: br.md, marginBottom: spacing.md }]}>
          <Text style={[styles.featuresTitle, { color: colors.muted }]}>Last Window (5s)</Text>
          <Text style={[styles.featureText, { color: colors.textSecondary }]}>
            Steps: {lastFeatures.stepCount}{'\n'}
            Avg Step Time: {lastFeatures.avgStepTime}s{'\n'}
            Peak Freq: {lastFeatures.peakFrequency}Hz{'\n'}
            Tremor (4-6Hz): {lastFeatures.tremorFrequency}{'\n'}
            Stability: {lastFeatures.movementStability}
          </Text>
        </View>
      )}

      {featureLog.length > 0 && (
        <View style={[styles.logCard, { backgroundColor: colors.card, borderRadius: br.md, marginBottom: spacing.xl }]}>
          <Text style={[styles.logTitle, { color: colors.muted }]}>
            Recent Windows ({featureLog.length} total)
          </Text>
          {featureLog.slice(0, 5).map((entry, i) => (
            <Text key={i} style={[styles.logEntry, { color: colors.textSecondary }]}>
              #{entry.window} — {entry.stepCount} steps, {entry.avgStepTime}s avg
            </Text>
          ))}
        </View>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { padding: 24, paddingTop: 60 },
  title: { fontSize: 28, fontWeight: '700', marginBottom: 20 },
  statusCard: { flexDirection: 'row', alignItems: 'center', padding: 16 },
  statusDot: { width: 12, height: 12, borderRadius: 6, marginRight: 10 },
  activeDot: { backgroundColor: '#48BB78' },
  inactiveDot: { backgroundColor: '#A0AEC0' },
  statusText: { fontSize: 16, fontWeight: '500' },
  sensorCard: { padding: 16 },
  sensorLabel: { fontSize: 14, fontWeight: '600', marginBottom: 8 },
  sensorValue: { fontSize: 14, fontFamily: 'monospace' },
  button: { borderRadius: 12, padding: 16, alignItems: 'center', marginBottom: 16 },
  startButton: { backgroundColor: '#48BB78' },
  stopButton: { backgroundColor: '#FC8181' },
  buttonText: { color: '#fff', fontSize: 16, fontWeight: '600' },
  featuresCard: { padding: 16 },
  featuresTitle: { fontSize: 14, fontWeight: '600', marginBottom: 8 },
  featureText: { fontSize: 14, fontFamily: 'monospace', lineHeight: 22 },
  logCard: { padding: 16 },
  logTitle: { fontSize: 14, fontWeight: '600', marginBottom: 8 },
  logEntry: { fontSize: 13, marginBottom: 4, fontFamily: 'monospace' },
  qualityCard: { padding: 16 },
  qualityTitle: { fontSize: 14, fontWeight: '600', marginBottom: 8 },
  qualityText: { fontSize: 14, fontWeight: '500', marginBottom: 4 },
  qualityDetail: { fontSize: 12, fontFamily: 'monospace' },
  baselineCard: { padding: 16 },
  baselineTitle: { fontSize: 14, fontWeight: '600', marginBottom: 8 },
  baselineDetail: { fontSize: 13, fontFamily: 'monospace' },
  deviationCard: { padding: 16 },
  deviationTitle: { fontSize: 14, fontWeight: '600', marginBottom: 8 },
  deviationClass: { fontSize: 16, fontWeight: '600', marginBottom: 8 },
  explanationText: { fontSize: 13, marginBottom: 4 },
});
