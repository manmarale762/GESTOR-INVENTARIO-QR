import React, { useMemo, useState } from 'react';
import { Alert, Pressable, StyleSheet, View } from 'react-native';
import { CameraView, useCameraPermissions } from 'expo-camera';
import { AppText } from '../../components/AppText';
import { Button } from '../../components/Button';
import { Card } from '../../components/Card';
import { Screen } from '../../components/Screen';
import { SectionHeader } from '../../components/SectionHeader';
import { APP_CONFIG } from '../../config/app';
import { inventory } from '../../data/mock';
import { api } from '../../services/api';
import { ScanValidationResponse } from '../../types';
import { colors, radii, spacing } from '../../theme';

const actions = [
  { key: 'access', label: 'Acceso' },
  { key: 'checkout', label: 'Retirada' },
  { key: 'return', label: 'Devolución' },
] as const;

export function ScannerScreen() {
  const [permission, requestPermission] = useCameraPermissions();
  const [selectedAction, setSelectedAction] = useState<(typeof actions)[number]['key']>('access');
  const [selectedTarget, setSelectedTarget] = useState(APP_CONFIG.defaultZone);
  const [scanLocked, setScanLocked] = useState(false);
  const [result, setResult] = useState<ScanValidationResponse | null>(null);

  const assetTargets = useMemo(() => inventory.map((item) => item.name), []);

  const handleScanned = async ({ data }: { data: string }) => {
    if (scanLocked) return;

    setScanLocked(true);
    try {
      const response = await api.validateScan({
        qrContent: data,
        action: selectedAction,
        target: selectedTarget,
      });
      setResult(response);
      Alert.alert(response.approved ? 'Operación aprobada' : 'Operación denegada', response.message);
    } catch (error) {
      Alert.alert('Error', error instanceof Error ? error.message : 'No se pudo validar el QR.');
    } finally {
      setTimeout(() => setScanLocked(false), 1800);
    }
  };

  if (!permission) {
    return <Screen><AppText>Cargando permisos de cámara…</AppText></Screen>;
  }

  if (!permission.granted) {
    return (
      <Screen>
        <SectionHeader title="Escáner QR" description="Se necesita permiso de cámara para leer los códigos dinámicos." />
        <Button label="Conceder permiso" onPress={() => requestPermission()} />
      </Screen>
    );
  }

  return (
    <Screen scrollable={false}>
      <View style={styles.container}>
        <View style={styles.topArea}>
          <SectionHeader title="Escáner QR" description="Valida accesos, retiradas y devoluciones en tiempo real." />
          <Card>
            <AppText variant="subtitle">Tipo de operación</AppText>
            <View style={styles.segmentRow}>
              {actions.map((action) => (
                <Pressable
                  key={action.key}
                  onPress={() => {
                    setSelectedAction(action.key);
                    setSelectedTarget(action.key === 'access' ? APP_CONFIG.defaultZone : assetTargets[0]);
                  }}
                  style={[styles.segment, selectedAction === action.key && styles.segmentActive]}
                >
                  <AppText style={selectedAction === action.key ? styles.segmentLabelActive : undefined}>{action.label}</AppText>
                </Pressable>
              ))}
            </View>
            <AppText variant="subtitle" style={{ marginTop: spacing.sm }}>Objetivo</AppText>
            <View style={styles.targetWrap}>
              {(selectedAction === 'access' ? [APP_CONFIG.defaultZone, 'Jaula de seguridad', 'Laboratorio técnico'] : assetTargets).map((target) => (
                <Pressable key={target} onPress={() => setSelectedTarget(target)} style={[styles.targetChip, selectedTarget === target && styles.targetChipActive]}>
                  <AppText style={selectedTarget === target ? styles.segmentLabelActive : undefined}>{target}</AppText>
                </Pressable>
              ))}
            </View>
          </Card>
        </View>

        <View style={styles.cameraShell}>
          <CameraView
            style={styles.camera}
            facing="back"
            onBarcodeScanned={handleScanned}
            barcodeScannerSettings={{ barcodeTypes: ['qr'] }}
          />
          <View pointerEvents="none" style={styles.overlay}>
            <View style={styles.scanFrame} />
          </View>
        </View>

        <Card>
          <AppText variant="subtitle">Último resultado</AppText>
          {result ? (
            <>
              <AppText>{result.message}</AppText>
              <AppText style={{ color: colors.textMuted }}>Trabajador: {result.record.workerName}</AppText>
              <AppText style={{ color: colors.textMuted }}>Acción: {selectedAction}</AppText>
              <AppText style={{ color: colors.textMuted }}>Objetivo: {result.record.target}</AppText>
            </>
          ) : (
            <AppText style={{ color: colors.textMuted }}>Todavía no se ha procesado ningún escaneo.</AppText>
          )}
        </Card>
      </View>
    </Screen>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: spacing.md,
    gap: spacing.md,
    backgroundColor: colors.background,
  },
  topArea: {
    gap: spacing.md,
  },
  cameraShell: {
    flex: 1,
    overflow: 'hidden',
    borderRadius: radii.lg,
    borderWidth: 1,
    borderColor: colors.border,
    minHeight: 260,
  },
  camera: {
    flex: 1,
  },
  overlay: {
    ...StyleSheet.absoluteFillObject,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(2,6,23,0.15)',
  },
  scanFrame: {
    width: 240,
    height: 240,
    borderRadius: 26,
    borderWidth: 3,
    borderColor: colors.primary,
    backgroundColor: 'transparent',
  },
  segmentRow: {
    flexDirection: 'row',
    gap: spacing.sm,
    flexWrap: 'wrap',
  },
  segment: {
    flex: 1,
    minWidth: 92,
    minHeight: 44,
    borderRadius: radii.pill,
    borderWidth: 1,
    borderColor: colors.border,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.surfaceAlt,
    paddingHorizontal: spacing.sm,
  },
  segmentActive: {
    backgroundColor: colors.primary,
    borderColor: colors.primary,
  },
  segmentLabelActive: {
    color: colors.white,
    fontWeight: '700',
  },
  targetWrap: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: spacing.sm,
  },
  targetChip: {
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.surfaceAlt,
    borderRadius: radii.pill,
    paddingHorizontal: spacing.md,
    paddingVertical: 10,
  },
  targetChipActive: {
    backgroundColor: colors.primary,
    borderColor: colors.primary,
  },
});
