import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useAuth } from '../context/AuthContext';
import { getTodayScore, getScoreHistory, getScoreSummary, getBaseline } from '../api/client';
import { useTheme } from '../theme';
import {
  ScoreCard,
  BaselineProgress,
  PeriodToggle,
  TrendChart,
  SummaryStats,
  DeviationList,
} from '../components';
import AsyncStorage from '@react-native-async-storage/async-storage';

function getGreeting() {
  const h = new Date().getHours();
  if (h < 12) return 'Good morning';
  if (h < 18) return 'Good afternoon';
  return 'Good evening';
}

export default function DashboardScreen() {
  const { user, isAuthenticated } = useAuth();
  const { colors, spacing } = useTheme();

  const [score, setScore] = useState(null);
  const [history, setHistory] = useState([]);
  const [summary, setSummary] = useState([]);
  const [baseline, setBaseline] = useState(null);
  const [localBaseline, setLocalBaseline] = useState(null);
  const [loading, setLoading] = useState(true);
  const [trendView, setTrendView] = useState('daily');

  useEffect(() => {
    if (isAuthenticated && user) {
      loadData();
      loadLocalBaseline();
    } else {
      setLoading(false);
    }
  }, [isAuthenticated, user]);

  async function loadLocalBaseline() {
    try {
      const stored = await AsyncStorage.getItem('adaptiveBaseline');
      if (stored) {
        const data = JSON.parse(stored);
        setLocalBaseline(data);
      }
    } catch (e) {
    }
  }

  useEffect(() => {
    if (isAuthenticated && user && trendView !== 'daily') {
      loadSummary();
    }
  }, [trendView]);

  async function loadData() {
    try {
      const [scoreData, historyData, baselineData] = await Promise.all([
        getTodayScore(user._id),
        getScoreHistory(user._id),
        getBaseline(user._id),
      ]);
      setScore(scoreData);
      setHistory(historyData.history || []);
      setBaseline(baselineData);
    } catch (e) {
    } finally {
      setLoading(false);
    }
  }

  async function loadSummary() {
    try {
      const summaryData = await getScoreSummary(user._id, trendView);
      setSummary(summaryData.data || []);
    } catch (e) {
    }
  }

  const scoreValue = score?.score ?? null;

  const historyLast7 = history.slice(0, 7).reverse();

  const trendData = trendView === 'daily'
    ? (historyLast7.length > 0 ? historyLast7 : null)
    : (summary.length > 0 ? summary : null);

  const scores = history.map((h) => h.score).filter((s) => s != null);
  const avgScore = scores.length > 0
    ? Math.round(scores.reduce((a, b) => a + b, 0) / scores.length)
    : null;
  const bestScore = scores.length > 0 ? Math.min(...scores) : null;
  const worstScore = scores.length > 0 ? Math.max(...scores) : null;

  if (loading) {
    return (
      <View style={[styles.center, { backgroundColor: colors.background }]}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  if (!isAuthenticated) {
    return (
      <View style={[styles.center, { backgroundColor: colors.background }]}>
        <Text style={[styles.noData, { color: colors.muted }]}>Sign in to view your dashboard</Text>
      </View>
    );
  }

  return (
    <ScrollView style={[styles.container, { backgroundColor: colors.background }]} contentContainerStyle={styles.content}>
      <View style={styles.header}>
        <View>
          <Text style={[styles.greeting, { color: colors.muted }]}>
            {getGreeting()}
          </Text>
          <Text style={[styles.title, { color: colors.text }]}>Dashboard</Text>
        </View>
        <TouchableOpacity onPress={loadData}>
          <Text style={[styles.refreshBtn, { color: colors.primary }]}>Refresh</Text>
        </TouchableOpacity>
      </View>

      {baseline && !baseline.establishedAt ? (
        <BaselineProgress
          daysCollected={baseline.daysCollected || 0}
          daysRequired={baseline.daysRequired || 7}
        />
      ) : (localBaseline && !localBaseline.ema ? (
        <BaselineProgress
          daysCollected={localBaseline.dayCount || 0}
          daysRequired={localBaseline.totalDays || 7}
        />
      ) : (
        <ScoreCard
          score={scoreValue}
          explanation={score?.explanation}
          recommendation={score?.recommendation}
        />
      ))}

      <PeriodToggle selected={trendView} onSelect={setTrendView} />

      {trendData ? (
        <TrendChart data={trendData} period={trendView} />
      ) : (
        <View style={[styles.noDataCard, { backgroundColor: colors.card }]}>
          <Text style={[styles.noData, { color: colors.muted }]}>No trend data available</Text>
        </View>
      )}

      <SummaryStats avg={avgScore} best={bestScore} worst={worstScore} />

      {score?.deviations && (
        <DeviationList deviations={score.deviations} />
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { padding: 24, paddingTop: 60, paddingBottom: 40 },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-end',
    marginBottom: 20,
  },
  greeting: { fontSize: 14, fontWeight: '500', marginBottom: 2 },
  title: { fontSize: 28, fontWeight: '700' },
  refreshBtn: { fontSize: 14, fontWeight: '600', paddingVertical: 4, paddingHorizontal: 8 },
  noData: { fontSize: 16, textAlign: 'center' },
  noDataCard: { padding: 40, borderRadius: 16, alignItems: 'center', marginBottom: 16 },
});
