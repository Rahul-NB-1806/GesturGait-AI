import { View, Text, StyleSheet } from 'react-native';
import { useTheme } from '../theme';

export default function SummaryStats({ avg, best, worst }) {
  const { colors, spacing, borderRadius: br } = useTheme();

  const items = [
    { label: 'Average', value: avg, color: colors.text },
    { label: 'Best', value: best, color: colors.success },
    { label: 'Worst', value: worst, color: colors.danger },
  ];

  return (
    <View style={[styles.container, { marginBottom: spacing.md }]}>
      {items.map((item, i) => (
        <View
          key={i}
          style={[styles.card, { backgroundColor: colors.card, borderRadius: br.md }]}
        >
          <Text style={[styles.label, { color: colors.muted }]}>{item.label}</Text>
          <Text style={[styles.value, { color: item.color }]}>
            {item.value !== null && item.value !== undefined ? item.value : '--'}
          </Text>
        </View>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flexDirection: 'row', gap: 8 },
  card: {
    flex: 1,
    padding: 16,
    alignItems: 'center',
  },
  label: { fontSize: 12, fontWeight: '500', marginBottom: 4 },
  value: { fontSize: 28, fontWeight: '700' },
});
