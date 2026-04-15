import React from 'react';
import { StyleSheet, View } from 'react-native';
import { colors, radii, spacing } from '../theme';
import { AppText } from './AppText';

export function BadgePill({ label, tone = 'neutral' }: { label: string; tone?: 'neutral' | 'success' | 'warning' | 'danger' | 'info' }) {
  return (
    <View style={[styles.pill, styles[tone]]}>
      <AppText style={styles.label}>{label}</AppText>
    </View>
  );
}

const styles = StyleSheet.create({
  pill: {
    alignSelf: 'flex-start',
    borderRadius: radii.pill,
    paddingHorizontal: spacing.sm,
    paddingVertical: 6,
  },
  neutral: { backgroundColor: '#1f2937' },
  success: { backgroundColor: 'rgba(34,197,94,0.16)' },
  warning: { backgroundColor: 'rgba(245,158,11,0.16)' },
  danger: { backgroundColor: 'rgba(239,68,68,0.16)' },
  info: { backgroundColor: 'rgba(59,130,246,0.16)' },
  label: {
    fontSize: 12,
    fontWeight: '700',
  },
});
