import React, { PropsWithChildren } from 'react';
import { StyleProp, StyleSheet, Text, TextStyle } from 'react-native';
import { colors } from '../theme';

interface Props extends PropsWithChildren {
  variant?: 'title' | 'subtitle' | 'body' | 'caption' | 'stat';
  style?: StyleProp<TextStyle>;
}

export function AppText({ children, variant = 'body', style }: Props) {
  return <Text style={[styles.base, styles[variant], style]}>{children}</Text>;
}

const styles = StyleSheet.create({
  base: {
    color: colors.text,
  },
  title: {
    fontSize: 28,
    fontWeight: '700',
  },
  subtitle: {
    fontSize: 18,
    fontWeight: '600',
  },
  body: {
    fontSize: 15,
    lineHeight: 22,
  },
  caption: {
    fontSize: 12,
    color: colors.textMuted,
  },
  stat: {
    fontSize: 30,
    fontWeight: '800',
  },
});
