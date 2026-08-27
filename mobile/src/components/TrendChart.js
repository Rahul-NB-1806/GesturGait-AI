import { View, Text, StyleSheet, Dimensions } from 'react-native';
import { VictoryChart, VictoryLine, VictoryBar, VictoryAxis, VictoryTooltip, VictoryVoronoiContainer } from 'victory-native';
import { useTheme } from '../theme';

const screenWidth = Dimensions.get('window').width;
const chartWidth = screenWidth - 72;

const MONTH_NAMES = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];

function formatLabel(key, period) {
  if (!key) return '';
  if (period === 'daily') return key.slice(0, 3);
  if (period === 'weekly') return `W${key.slice(-2)}`;
  const parts = key.split('-');
  const monthIdx = parseInt(parts[1], 10) - 1;
  return MONTH_NAMES[monthIdx] || key;
}

export default function TrendChart({ data, period }) {
  const { colors, spacing, borderRadius: br } = useTheme();

  if (!data || data.length === 0) {
    return (
      <View style={[styles.emptyCard, { backgroundColor: colors.card, borderRadius: br.lg, marginBottom: spacing.md }]}>
        <Text style={[styles.emptyText, { color: colors.muted }]}>No data yet</Text>
      </View>
    );
  }

  const chartData = data.map((d) => ({
    label: period === 'daily'
      ? new Date(d.date).toLocaleDateString(undefined, { weekday: 'short' })
      : d.label,
    score: d.avgScore ?? d.score,
    date: d.date,
  }));

  const isLine = period === 'daily';

  return (
    <View style={[styles.card, { backgroundColor: colors.card, borderRadius: br.lg, marginBottom: spacing.md }]}>
      <Text style={[styles.title, { color: colors.text }]}>Trend</Text>
      <VictoryChart
        width={chartWidth}
        height={200}
        padding={{ top: 20, bottom: 30, left: 40, right: 20 }}
        containerComponent={
          <VictoryVoronoiContainer
            labels={({ datum }) => `${datum.label}: ${datum.score}`}
            labelComponent={
              <VictoryTooltip
                flyoutStyle={{ fill: colors.card, stroke: colors.border, strokeWidth: 1 }}
                style={{ fill: colors.text, fontSize: 12 }}
              />
            }
          />
        }
      >
        <VictoryAxis
          style={{
            tickLabels: { fill: colors.muted, fontSize: 10 },
            axis: { stroke: colors.border },
            grid: { stroke: 'transparent' },
          }}
          tickFormat={(t) => formatLabel(t, period)}
        />
        <VictoryAxis
          dependentAxis
          style={{
            tickLabels: { fill: colors.muted, fontSize: 10 },
            axis: { stroke: 'transparent' },
            grid: { stroke: colors.border, strokeDasharray: '4' },
          }}
          tickFormat={(t) => `${t}`}
        />
        {isLine ? (
          <VictoryLine
            data={chartData}
            x="label"
            y="score"
            style={{
              data: { stroke: colors.primary, strokeWidth: 2 },
            }}
            interpolation="monotoneX"
          />
        ) : (
          <VictoryBar
            data={chartData}
            x="label"
            y="score"
            style={{
              data: { fill: colors.primary, width: 24 },
            }}
          />
        )}
      </VictoryChart>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { padding: 20 },
  title: { fontSize: 16, fontWeight: '600', marginBottom: 8 },
  emptyCard: { padding: 40, alignItems: 'center' },
  emptyText: { fontSize: 14 },
});
