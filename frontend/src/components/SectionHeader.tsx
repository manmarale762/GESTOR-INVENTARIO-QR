import React from 'react';
import { StyleSheet, View } from 'react-native';
import { spacing } from '../theme';
import { AppText } from './AppText';

export function SectionHeader({ title, description }: { title: string; description?: string }) {
  return (
    <View style={styles.wrapper}>
      <AppText variant="subtitle">{title}</AppText>
      {description ? <AppText>{description}</AppText> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    gap: spacing.xs,
  },
});
