import React, { useEffect, useMemo, useState } from 'react';
import { StyleSheet, View } from 'react-native';
import QRCode from 'react-native-qrcode-svg';
import { APP_CONFIG } from '../../config/app';
import { useAuth } from '../../context/AuthContext';
import { AppText } from '../../components/AppText';
import { Card } from '../../components/Card';
import { Screen } from '../../components/Screen';
import { BadgePill } from '../../components/BadgePill';
import { colors, spacing } from '../../theme';
import { buildDynamicQrPayload, encodeDynamicQr } from '../../services/qr';

export function DynamicBadgeScreen() {
  const { session } = useAuth();
  const [now, setNow] = useState(Date.now());

  useEffect(() => {
    const timer = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(timer);
  }, []);

  const payload = useMemo(() => (session ? buildDynamicQrPayload(session.user, now) : null), [session, now]);
  const qrValue = useMemo(() => (session ? encodeDynamicQr(session.user, now) : ''), [session, now]);

  if (!session || !payload) {
    return null;
  }

  const secondsLeft = Math.max(0, Math.ceil((payload.expiresAt - now) / 1000));
  const progress = secondsLeft / APP_CONFIG.qrRefreshIntervalSeconds;

  return (
    <Screen>
      <Card style={styles.centered}>
        <BadgePill label="Credencial dinámica" tone="info" />
        <AppText variant="subtitle">{session.user.fullName}</AppText>
        <AppText style={styles.meta}>{session.user.employeeCode} · {session.user.department}</AppText>
        <View style={styles.qrWrapper}>
          <QRCode value={qrValue} size={220} color={colors.white} backgroundColor={colors.surfaceAlt} />
        </View>
        <AppText variant="stat" style={{ fontSize: 22 }}>{payload.token}</AppText>
        <View style={styles.progressTrack}>
          <View style={[styles.progressFill, { width: `${progress * 100}%` }]} />
        </View>
        <AppText style={styles.meta}>Caduca en {secondsLeft}s · Ventana TOTP: {APP_CONFIG.qrRefreshIntervalSeconds}s</AppText>
      </Card>

      <Card>
        <AppText variant="subtitle">Datos de acceso</AppText>
        <AppText style={styles.meta}>Zona autorizada: {session.user.zoneName}</AppText>
        <AppText style={styles.meta}>Emitido: {new Date(payload.issuedAt).toLocaleTimeString('es-ES')}</AppText>
        <AppText style={styles.meta}>Expira: {new Date(payload.expiresAt).toLocaleTimeString('es-ES')}</AppText>
      </Card>
    </Screen>
  );
}

const styles = StyleSheet.create({
  centered: {
    alignItems: 'center',
  },
  meta: {
    color: colors.textMuted,
    textAlign: 'center',
  },
  qrWrapper: {
    padding: spacing.md,
    borderRadius: 24,
    backgroundColor: colors.surfaceAlt,
  },
  progressTrack: {
    width: '100%',
    height: 10,
    borderRadius: 999,
    backgroundColor: colors.surfaceAlt,
    overflow: 'hidden',
  },
  progressFill: {
    height: '100%',
    backgroundColor: colors.primary,
  },
});
