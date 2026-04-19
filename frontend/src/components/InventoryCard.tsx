import React from 'react';
import { StyleSheet, View } from 'react-native';
import { InventoryItem } from '../types';
import { colors, spacing } from '../theme';
import { AppText } from './AppText';
import { BadgePill } from './BadgePill';
import { Card } from './Card';

export function InventoryCard({ item }: { item: InventoryItem }) {
  const tone = item.status === 'available' ? 'success' : item.status === 'loaned' ? 'warning' : item.status === 'maintenance' ? 'danger' : 'info';

  return (
    <Card>
      <View style={styles.row}>
        <View style={{ flex: 1, gap: spacing.xs }}>
          <AppText variant="subtitle">{item.name}</AppText>
          <AppText style={styles.meta}>{item.category}</AppText>
        </View>
        <BadgePill label={item.status.toUpperCase()} tone={tone} />
      </View>
      <AppText style={styles.meta}>Serie: {item.serialNumber}</AppText>
      <AppText style={styles.meta}>Ubicación: {item.location}</AppText>
      <AppText style={styles.meta}>Cantidad: {item.quantity}</AppText>
      {item.assignedTo ? <AppText style={styles.meta}>Asignado a: {item.assignedTo}</AppText> : null}
      {item.highValue ? <BadgePill label="Activo de alto valor" tone="info" /> : null}
    </Card>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    gap: spacing.sm,
    alignItems: 'center',
  },
  meta: {
    color: colors.textMuted,
  },
});
