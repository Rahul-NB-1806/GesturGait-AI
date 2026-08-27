import { View, Text, StyleSheet } from 'react-native';
import { useTheme } from '../theme';

export default function BaselineProgress({ daysCollected = 0, daysRequired = 7 }) {
  const { colors, spacing, borderRadius: br } = useTheme();
  const progress = Math.min(1, daysCollected / daysRequired);

  return (
    <View
      style={[
        styles.card,
        {
          backgroundColor: colors.primaryLight,
          borderRadius: br.lg,
          marginBottom: spacing.md,
        },
      ]}
    >
      <Text style={[styles.title, { color: colors.primary }]}>
        Establishing your baseline...
      </Text>
      <View style={[styles.track, { backgroundColor: colors.gaugeTrack, borderRadius: br.sm }]}>
        <View
          style={[
            styles.fill,
            {
              width: `${progress * 100}%`,
              backgroundColor: colors.primary,
              borderRadius: br.sm,
            },
          ]}
        />
      </View>
      <Text style={[styles.text, { color: colors.primary }]}>
        Day {daysCollected} of {daysRequired}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { padding: 20 },
  title: { fontSize: 16, fontWeight: '600', marginBottom: 12 },
  track: { height: 8, marginBottom: 8, overflow: 'hidden' },
  fill: { height: '100%' },
  text: { fontSize: 14 },
});
