import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView } from 'react-native';

export default function OnboardingScreen({ navigation }) {
  const privacyPoints = [
    'Only derived numerical features leave your device — never raw sensor data',
    'No typed text, passwords, or screen content is ever captured or stored',
    'All data is transmitted over HTTPS with encrypted authentication',
    'You can export or delete all your stored data at any time',
    'This is a screening aid only — it does not provide medical diagnoses',
  ];

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text style={styles.title}>Welcome to GesturGait AI</Text>
      <Text style={styles.subtitle}>
        A passive monitoring system that helps detect early signs of
        Parkinson's-related motor decline by analyzing your natural phone
        use patterns.
      </Text>

      <View style={styles.card}>
        <Text style={styles.cardTitle}>How It Works</Text>
        <Text style={styles.cardText}>
          GesturGait AI monitors your phone's motion sensors while you walk,
          type, and swipe — building a personalized "Digital Motor Baseline"
          over 7 days. It then detects meaningful deviations from your own
          baseline and alerts you if consultation may be warranted.
        </Text>
      </View>

      <View style={styles.card}>
        <Text style={styles.cardTitle}>Privacy First</Text>
        {privacyPoints.map((point, i) => (
          <View key={i} style={styles.bulletRow}>
            <Text style={styles.bullet}>•</Text>
            <Text style={styles.bulletText}>{point}</Text>
          </View>
        ))}
      </View>

      <View style={styles.card}>
        <Text style={styles.cardTitle}>Required Permissions</Text>
        <View style={styles.bulletRow}>
          <Text style={styles.bullet}>•</Text>
          <Text style={styles.bulletText}>
            Motion sensors (accelerometer + gyroscope) — for gait and
            movement analysis
          </Text>
        </View>
        <View style={styles.bulletRow}>
          <Text style={styles.bullet}>•</Text>
          <Text style={styles.bulletText}>
            (Optional) Activity recognition — for identifying walking periods
          </Text>
        </View>
      </View>

      <Text style={styles.disclaimer}>
        ⚠ This tool is a screening aid only. It is not a diagnostic device
        and does not replace medical professional consultation.
      </Text>

      <TouchableOpacity
        style={styles.button}
        onPress={() => navigation.replace('App')}
      >
        <Text style={styles.buttonText}>I Understand, Let's Start</Text>
      </TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F7F9FC',
  },
  content: {
    padding: 24,
    paddingTop: 60,
  },
  title: {
    fontSize: 28,
    fontWeight: '700',
    color: '#1A365D',
    textAlign: 'center',
    marginBottom: 12,
  },
  subtitle: {
    fontSize: 15,
    color: '#4A5568',
    textAlign: 'center',
    lineHeight: 22,
    marginBottom: 24,
  },
  card: {
    backgroundColor: '#fff',
    borderRadius: 16,
    padding: 20,
    marginBottom: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 8,
    elevation: 2,
  },
  cardTitle: {
    fontSize: 17,
    fontWeight: '600',
    color: '#1A365D',
    marginBottom: 10,
  },
  cardText: {
    fontSize: 14,
    color: '#4A5568',
    lineHeight: 20,
  },
  bulletRow: {
    flexDirection: 'row',
    marginBottom: 6,
  },
  bullet: {
    fontSize: 14,
    color: '#4299E1',
    marginRight: 8,
    lineHeight: 20,
  },
  bulletText: {
    fontSize: 14,
    color: '#4A5568',
    flex: 1,
    lineHeight: 20,
  },
  disclaimer: {
    fontSize: 12,
    color: '#A0AEC0',
    textAlign: 'center',
    marginBottom: 24,
    lineHeight: 18,
  },
  button: {
    backgroundColor: '#4299E1',
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
    marginBottom: 40,
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
});
