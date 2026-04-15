import React from 'react';
import { Alert, StyleSheet, View } from 'react-native';
import { APP_CONFIG } from '../../config/app';
import { useAuth } from '../../context/AuthContext';
import { AppText } from '../../components/AppText';
import { Button } from '../../components/Button';
import { Card } from '../../components/Card';
import { Screen } from '../../components/Screen';
import { BadgePill } from '../../components/BadgePill';
import { colors, spacing } from '../../theme';

export function ProfileScreen() {
  const { session, signOut } = useAuth();

  if (!session) return null;

  return (
    <Screen>
      <Card>
        <View style={styles.avatar}>
          <AppText variant="subtitle">{session.user.fullName.slice(0, 1)}</AppText>
        </View>
        <AppText variant="title" style={{ fontSize: 24 }}>{session.user.fullName}</AppText>
        <BadgePill label={session.user.role === 'admin' ? 'Administrador' : 'Trabajador'} tone="info" />
        <AppText style={styles.meta}>{session.user.email}</AppText>
        <AppText style={styles.meta}>Código: {session.user.employeeCode}</AppText>
        <AppText style={styles.meta}>Departamento: {session.user.department}</AppText>
        <AppText style={styles.meta}>Zona: {session.user.zoneName}</AppText>
      </Card>

      <Card>
        <AppText variant="subtitle">Configuración técnica</AppText>
        <AppText style={styles.meta}>API mock: {APP_CONFIG.useMockApi ? 'Sí' : 'No'}</AppText>
        <AppText style={styles.meta}>Base URL: {APP_CONFIG.apiBaseUrl}</AppText>
        <AppText style={styles.meta}>Renovación QR: cada {APP_CONFIG.qrRefreshIntervalSeconds}s</AppText>
      </Card>

      <Button
        label="Cerrar sesión"
        variant="danger"
        onPress={() => {
          Alert.alert('Cerrar sesión', 'Se cerrará la sesión actual.', [
            { text: 'Cancelar', style: 'cancel' },
            { text: 'Salir', style: 'destructive', onPress: () => signOut() },
          ]);
        }}
      />
    </Screen>
  );
}

const styles = StyleSheet.create({
  avatar: {
    width: 72,
    height: 72,
    borderRadius: 999,
    backgroundColor: colors.surfaceAlt,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: spacing.sm,
  },
  meta: {
    color: colors.textMuted,
  },
});
