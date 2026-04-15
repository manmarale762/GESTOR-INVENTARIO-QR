import React from 'react';
import { StyleSheet, TextInput, View } from 'react-native';
import { colors, radii, spacing } from '../theme';
import { AppText } from './AppText';

interface Props {
  label: string;
  value: string;
  onChangeText: (value: string) => void;
  secureTextEntry?: boolean;
  autoCapitalize?: 'none' | 'sentences' | 'words' | 'characters';
  keyboardType?: 'default' | 'email-address';
  placeholder?: string;
}

export function FormField({ label, ...props }: Props) {
  return (
    <View style={styles.wrapper}>
      <AppText style={styles.label}>{label}</AppText>
      <TextInput
        placeholderTextColor={colors.textMuted}
        style={styles.input}
        {...props}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    gap: spacing.xs,
  },
  label: {
    fontSize: 13,
    color: colors.textMuted,
  },
  input: {
    minHeight: 52,
    borderRadius: radii.md,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.surfaceAlt,
    color: colors.text,
    paddingHorizontal: spacing.md,
  },
});
