import React from 'react';
import { StyleSheet, View } from 'react-native';
import { ScanRecord } from '../types';
import { colors, spacing } from '../theme';
import { AppText } from './AppText';
import { BadgePill } from './BadgePill';
import { Card } from './Card';

export function HistoryCard({ record }: { record: ScanRecord }) {
  return (
    <Card>
      <View style={styles.row}>
        <AppText variant="subtitle">{record.workerName}</AppText>
        <BadgePill label={record.result === 'approved' ? 'APROBADO' : 'DENEGADO'} tone={record.result === 'approved' ? 'success' : 'danger'} />
      </View>
      <AppText style={styles.meta}>Acción: {translateAction(record.action)}</AppText>
      <AppText style={styles.meta}>Objetivo: {record.target}</AppText>
      <AppText style={styles.meta}>Fecha: {formatDate(record.timestamp)}</AppText>
      {record.reason ? <AppText style={styles.reason}>Motivo: {record.reason}</AppText> : null}
    </Card>
  );
}

function translateAction(action: ScanRecord['action']) {
  return action === 'access' ? 'Acceso' : action === 'checkout' ? 'Retirada' : 'Devolución';
}

function formatDate(value: string) {
  return new Date(value).toLocaleString('es-ES', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: spacing.sm,
    alignItems: 'center',
  },
  meta: {
    color: colors.textMuted,
  },
  reason: {
    color: colors.warning,
  },
});
