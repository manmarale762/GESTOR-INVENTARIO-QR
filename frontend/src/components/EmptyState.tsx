import React from 'react';
import { StyleSheet, View } from 'react-native';
import { colors, spacing } from '../theme';
import { AppText } from './AppText';

export function EmptyState({ title, message }: { title: string; message: string }) {
  return (
    <View style={styles.box}>
      <AppText variant="subtitle">{title}</AppText>
      <AppText style={styles.message}>{message}</AppText>
    </View>
  );
}

const styles = StyleSheet.create({
  box: {
    borderWidth: 1,
    borderColor: colors.border,
    borderStyle: 'dashed',
    borderRadius: 18,
    padding: spacing.lg,
    gap: spacing.sm,
  },
  message: {
    color: colors.textMuted,
  },
});
