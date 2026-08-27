import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { useTheme } from '../theme';

export default function DeviationList({ deviations }) {
  const { colors, spacing, borderRadius: br } = useTheme();

  if (!deviations || deviations.length === 0) return null;

  return (
    <View style={[styles.card, { backgroundColor: colors.card, borderRadius: br.lg, marginBottom: spacing.md }]}>
      <Text style={[styles.title, { color: colors.text }]}>Feature Deviations</Text>
      {deviations.map((d, i) => {
        const isWorse = d.direction === 'worse';
        return (
          <View
            key={i}
            style={[
              styles.row,
              { borderBottomColor: colors.border },
              i === deviations.length - 1 && { borderBottomWidth: 0 },
            ]}
          >
            <Text style={[styles.featureName, { color: colors.textSecondary }]}>
              {d.feature}
            </Text>
            <View style={styles.valueRow}>
              <Text style={[styles.arrow, { color: isWorse ? colors.danger : colors.success }]}>
                {isWorse ? '↓' : '↑'}
              </Text>
              <Text
                style={[
                  styles.percent,
                  { color: isWorse ? colors.danger : colors.success },
                ]}
              >
                {d.deltaPercent > 0 ? '+' : ''}{d.deltaPercent.toFixed(1)}%
              </Text>
            </View>
          </View>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  card: { padding: 20 },
  title: { fontSize: 16, fontWeight: '600', marginBottom: 12 },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 10,
    borderBottomWidth: 1,
  },
  featureName: { fontSize: 14, flex: 1 },
  valueRow: { flexDirection: 'row', alignItems: 'center', gap: 6 },
  arrow: { fontSize: 16 },
  percent: { fontSize: 14, fontWeight: '600' },
});
