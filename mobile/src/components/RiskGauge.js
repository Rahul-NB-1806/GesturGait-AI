import { useMemo } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import Svg, { Path, Defs, LinearGradient, Stop } from 'react-native-svg';
import { useTheme } from '../theme';

function describeArc(cx, cy, r, startAngle, endAngle) {
  const s = polarToCartesian(cx, cy, r, endAngle);
  const e = polarToCartesian(cx, cy, r, startAngle);
  const largeArc = endAngle - startAngle <= 180 ? '0' : '1';
  return `M ${s.x} ${s.y} A ${r} ${r} 0 ${largeArc} 0 ${e.x} ${e.y}`;
}

function polarToCartesian(cx, cy, r, angleDeg) {
  const rad = ((angleDeg - 90) * Math.PI) / 180;
  return { x: cx + r * Math.cos(rad), y: cy + r * Math.sin(rad) };
}

function getScoreColor(score, colors) {
  if (score === null) return colors.muted;
  if (score < 30) return colors.success;
  if (score < 60) return colors.warning;
  return colors.danger;
}

function getScoreLabel(score) {
  if (score === null) return '--';
  if (score < 20) return 'Low';
  if (score < 40) return 'Moderate';
  if (score < 65) return 'Elevated';
  return 'High';
}

export default function RiskGauge({ score, size = 200, strokeWidth = 16 }) {
  const { colors } = useTheme();

  const radius = (size - strokeWidth) / 2;
  const cx = size / 2;
  const cy = size / 2;
  const startAngle = 135;
  const endAngle = 405;

  const progressFraction = score !== null ? Math.min(score, 100) / 100 : 0;
  const progressAngle = startAngle + (endAngle - startAngle) * progressFraction;

  const arcPath = describeArc(cx, cy, radius, startAngle, endAngle);
  const progressPath = describeArc(cx, cy, radius, startAngle, progressAngle);

  const scoreColor = getScoreColor(score, colors);
  const label = getScoreLabel(score);

  return (
    <View style={[styles.container, { width: size, height: size }]}>
      <Svg width={size} height={size}>
        <Defs>
          <LinearGradient id="gaugeGrad" x1="0%" y1="0%" x2="100%" y2="0%">
            <Stop offset="0%" stopColor={colors.success} />
            <Stop offset="50%" stopColor={colors.warning} />
            <Stop offset="100%" stopColor={colors.danger} />
          </LinearGradient>
        </Defs>
        <Path
          d={arcPath}
          fill="none"
          stroke={colors.gaugeTrack}
          strokeWidth={strokeWidth}
          strokeLinecap="round"
        />
        {score !== null && (
          <Path
            d={progressPath}
            fill="none"
            stroke="url(#gaugeGrad)"
            strokeWidth={strokeWidth}
            strokeLinecap="round"
          />
        )}
      </Svg>
      <View style={[styles.centerContent, { width: size, height: size }]}>
        <Text style={[styles.scoreText, { color: scoreColor }]}>
          {score !== null ? score : '--'}
        </Text>
        <Text style={[styles.labelText, { color: colors.muted }]}>{label}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { alignItems: 'center', justifyContent: 'center' },
  centerContent: {
    position: 'absolute',
    alignItems: 'center',
    justifyContent: 'center',
  },
  scoreText: { fontSize: 48, fontWeight: '700', lineHeight: 52 },
  labelText: { fontSize: 14, fontWeight: '500', marginTop: 4 },
});
