import React, { useState, useEffect } from 'react';
import {
  View, Text, StyleSheet, ScrollView, ActivityIndicator,
} from 'react-native';
import { useAuth } from '../context/AuthContext';
import { getScoreHistory } from '../api/client';
import { useTheme } from '../theme';
import { DeviationList } from '../components';

export default function HistoryScreen() {
  const { user, isAuthenticated } = useAuth();
  const { colors, spacing, borderRadius: br } = useTheme();
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (isAuthenticated && user) {
      loadHistory();
    } else {
      setLoading(false);
    }
  }, [isAuthenticated, user]);

  async function loadHistory() {
    try {
      const data = await getScoreHistory(user._id);
      setHistory(data.history || []);
    } catch (e) {
    } finally {
      setLoading(false);
    }
  }

  if (loading) {
    return (
      <View style={[styles.center, { backgroundColor: colors.background }]}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  function getScoreColor(s) {
    if (s < 30) return colors.success;
    if (s < 60) return colors.warning;
    return colors.danger;
  }

  return (
    <ScrollView style={[styles.container, { backgroundColor: colors.background }]} contentContainerStyle={styles.content}>
      <Text style={[styles.title, { color: colors.text }]}>History</Text>

      {history.length === 0 ? (
        <Text style={[styles.noData, { color: colors.muted }]}>No history available yet</Text>
      ) : (
        history.slice().reverse().map((entry, i) => {
          const date = new Date(entry.date).toLocaleDateString(undefined, {
            weekday: 'short',
            year: 'numeric',
            month: 'short',
            day: 'numeric',
          });
          return (
            <View key={i} style={[styles.card, { backgroundColor: colors.card, borderRadius: br.md, marginBottom: spacing.sm }]}>
              <View style={styles.cardHeader}>
                <Text style={[styles.cardDate, { color: colors.text }]}>{date}</Text>
                <Text style={[styles.cardScore, { color: getScoreColor(entry.score) }]}>
                  {entry.score}
                </Text>
              </View>
              {entry.explanation && (
                <Text style={[styles.cardExplanation, { color: colors.textSecondary }]}>
                  {entry.explanation}
                </Text>
              )}
              {entry.recommendation && (
                <Text style={[styles.cardRecommendation, { color: colors.primary }]}>
                  {entry.recommendation}
                </Text>
              )}
              <DeviationList deviations={entry.deviations} />
            </View>
          );
        })
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { padding: 24, paddingTop: 60 },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  title: { fontSize: 28, fontWeight: '700', marginBottom: 20 },
  noData: { fontSize: 16, textAlign: 'center', marginTop: 40 },
  card: { padding: 16 },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  cardDate: { fontSize: 15, fontWeight: '600' },
  cardScore: { fontSize: 24, fontWeight: '700' },
  cardExplanation: { fontSize: 14, marginBottom: 4 },
  cardRecommendation: { fontSize: 13, marginBottom: 8 },
});
