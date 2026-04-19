import React, { useEffect, useState } from 'react';
import { StyleSheet, View } from 'react-native';
import { useAuth } from '../../context/AuthContext';
import { Card } from '../../components/Card';
import { Screen } from '../../components/Screen';
import { AppText } from '../../components/AppText';
import { StatCard } from '../../components/StatCard';
import { SectionHeader } from '../../components/SectionHeader';
import { api } from '../../services/api';
import { Loan } from '../../types';
import { colors, spacing } from '../../theme';

export function WorkerHomeScreen() {
  const { session } = useAuth();
  const [loans, setLoans] = useState<Loan[]>([]);

  useEffect(() => {
    if (!session) return;
    api.getLoans(session.user.id).then(setLoans);
  }, [session]);

  const active = loans.filter((loan) => loan.status === 'active').length;
  const overdue = loans.filter((loan) => loan.status === 'overdue').length;

  return (
    <Screen>
      <SectionHeader title={`Hola, ${session?.user.fullName ?? ''}`} description="Consulta tu material asignado y proyecta tu acreditación segura." />

      <View style={styles.statsRow}>
        <StatCard label="Préstamos activos" value={active} />
        <StatCard label="Vencidos" value={overdue} />
      </View>

      <Card>
        <AppText variant="subtitle">Tu acceso actual</AppText>
        <AppText style={styles.meta}>Código empleado: {session?.user.employeeCode}</AppText>
        <AppText style={styles.meta}>Zona principal: {session?.user.zoneName}</AppText>
        <AppText style={styles.meta}>Departamento: {session?.user.department}</AppText>
      </Card>

      <Card>
        <AppText variant="subtitle">Cómo usar la app</AppText>
        <AppText style={styles.meta}>1. Entra en la pestaña Credencial.</AppText>
        <AppText style={styles.meta}>2. Muestra el QR en el control de acceso o punto de préstamo.</AppText>
        <AppText style={styles.meta}>3. Consulta tus materiales activos en la pestaña Préstamos.</AppText>
      </Card>

      <Card>
        <AppText variant="subtitle">Seguridad</AppText>
        <AppText style={styles.meta}>
          El QR cambia automáticamente cada pocos segundos y está pensado para validarse en tiempo real contra el backend.
        </AppText>
      </Card>
    </Screen>
  );
}

const styles = StyleSheet.create({
  statsRow: {
    flexDirection: 'row',
    gap: spacing.md,
  },
  meta: {
    color: colors.textMuted,
  },
});
