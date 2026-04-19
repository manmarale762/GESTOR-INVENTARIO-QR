import React from 'react';
import { StyleSheet, View } from 'react-native';
import { Loan } from '../types';
import { colors, spacing } from '../theme';
import { AppText } from './AppText';
import { BadgePill } from './BadgePill';
import { Card } from './Card';

export function LoanCard({ loan }: { loan: Loan }) {
  const tone = loan.status === 'overdue' ? 'danger' : loan.status === 'returned' ? 'neutral' : 'success';
  const label = loan.status === 'overdue' ? 'Vencido' : loan.status === 'returned' ? 'Devuelto' : 'Activo';

  return (
    <Card>
      <View style={styles.headerRow}>
        <AppText variant="subtitle">{loan.itemName}</AppText>
        <BadgePill label={label} tone={tone} />
      </View>
      <AppText style={styles.meta}>Serie: {loan.serialNumber}</AppText>
      <AppText style={styles.meta}>Retirado: {formatDate(loan.borrowedAt)}</AppText>
      <AppText style={styles.meta}>Vencimiento: {formatDate(loan.dueAt)}</AppText>
      <AppText style={styles.meta}>Ubicación: {loan.location}</AppText>
    </Card>
  );
}

function formatDate(value: string) {
  return new Date(value).toLocaleString('es-ES', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

const styles = StyleSheet.create({
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: spacing.sm,
  },
  meta: {
    color: colors.textMuted,
  },
});
