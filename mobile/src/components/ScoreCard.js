import { View, Text, StyleSheet } from 'react-native';
import { useTheme } from '../theme';
import RiskGauge from './RiskGauge';
import RecommendationBanner from './RecommendationBanner';

export default function ScoreCard({ score, explanation, recommendation }) {
  const { colors, spacing } = useTheme();

  return (
    <View style={[styles.card, { backgroundColor: colors.card, marginBottom: spacing.md }]}>
      <Text style={[styles.label, { color: colors.muted }]}>Today's Risk Score</Text>
      <View style={styles.gaugeWrap}>
        <RiskGauge score={score} />
      </View>
      <Text style={[styles.rangeHint, { color: colors.muted }]}>
        0 (low risk) — 100 (high risk)
      </Text>
      {explanation && (
        <Text style={[styles.explanation, { color: colors.textSecondary }]}>
          {explanation}
        </Text>
      )}
      {recommendation && <RecommendationBanner text={recommendation} score={score} />}
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: 16,
    padding: 24,
    alignItems: 'center',
  },
  label: { fontSize: 14, fontWeight: '500', marginBottom: 16 },
  gaugeWrap: { alignItems: 'center', marginBottom: 12 },
  rangeHint: { fontSize: 12, marginBottom: 12 },
  explanation: {
    fontSize: 14,
    textAlign: 'center',
    lineHeight: 20,
    marginBottom: 12,
  },
});
