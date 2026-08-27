import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { useTheme } from '../theme';

const PERIODS = [
  { key: 'daily', label: 'Daily' },
  { key: 'weekly', label: 'Weekly' },
  { key: 'monthly', label: 'Monthly' },
];

export default function PeriodToggle({ selected, onSelect }) {
  const { colors, borderRadius: br } = useTheme();

  return (
    <View style={[styles.container, { backgroundColor: colors.surface, borderRadius: br.sm, borderColor: colors.border }]}>
      {PERIODS.map((p) => (
        <TouchableOpacity
          key={p.key}
          style={[
            styles.btn,
            selected === p.key && { backgroundColor: colors.primary },
          ]}
          onPress={() => onSelect(p.key)}
        >
          <Text
            style={[
              styles.btnText,
              { color: colors.muted },
              selected === p.key && { color: '#fff', fontWeight: '600' },
            ]}
          >
            {p.label}
          </Text>
        </TouchableOpacity>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    borderRadius: 8,
    borderWidth: 1,
    padding: 3,
    marginBottom: 16,
  },
  btn: {
    flex: 1,
    paddingVertical: 8,
    paddingHorizontal: 12,
    borderRadius: 6,
    alignItems: 'center',
  },
  btnText: { fontSize: 13, fontWeight: '500' },
});
