import React, { useEffect, useState } from 'react';
import { StyleSheet, View } from 'react-native';
import { AppText } from '../../components/AppText';
import { Card } from '../../components/Card';
import { Screen } from '../../components/Screen';
import { SectionHeader } from '../../components/SectionHeader';
import { StatCard } from '../../components/StatCard';
import { api } from '../../services/api';
import { DashboardMetrics } from '../../types';
import { spacing } from '../../theme';

export function AdminDashboardScreen() {
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);

  useEffect(() => {
    api.getDashboardMetrics().then(setMetrics);
  }, []);

  return (
    <Screen>
      <SectionHeader title="Panel de control" description="Visión rápida del sistema de accesos y movimientos de inventario." />
      <View style={styles.row}>
        <StatCard label="Préstamos activos" value={metrics?.activeLoans ?? 0} />
        <StatCard label="Escaneos hoy" value={metrics?.todayScans ?? 0} />
      </View>
      <View style={styles.row}>
        <StatCard label="Activos disponibles" value={metrics?.availableAssets ?? 0} />
        <StatCard label="Zonas restringidas" value={metrics?.restrictedZones ?? 0} />
      </View>
      <Card>
        <AppText variant="subtitle">Uso recomendado</AppText>
        <AppText>1. Selecciona el tipo de operación en la pestaña Escáner.</AppText>
        <AppText>2. Escanea el QR dinámico del trabajador.</AppText>
        <AppText>3. Registra el acceso o movimiento de material y consulta el historial.</AppText>
      </Card>
    </Screen>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    gap: spacing.md,
  },
});
