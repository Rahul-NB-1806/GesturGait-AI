import { View, Text, StyleSheet } from 'react-native';
import { useTheme } from '../theme';

export default function RecommendationBanner({ text, score }) {
  const { colors, borderRadius: br } = useTheme();

  if (!text) return null;

  let bgColor = colors.primaryLight;
  if (score !== null && score >= 60) {
    bgColor = '#FFF5F5';
  } else if (score !== null && score >= 30) {
    bgColor = '#FFFFF0';
  }

  let iconColor = colors.primary;
  if (score !== null && score >= 60) {
    iconColor = colors.danger;
  } else if (score !== null && score >= 30) {
    iconColor = colors.warning;
  }

  const icon = score !== null && score >= 60 ? '⚠' : 'ℹ';

  return (
    <View style={[styles.banner, { backgroundColor: bgColor, borderRadius: br.sm }]}>
      <Text style={[styles.icon, { color: iconColor }]}>{icon}</Text>
      <Text style={[styles.text, { color: iconColor }]}>{text}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  banner: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    padding: 12,
    gap: 8,
  },
  icon: { fontSize: 16, marginTop: 1 },
  text: { fontSize: 13, lineHeight: 18, flex: 1 },
});
