import React, { useState } from 'react';
import { Alert, StyleSheet, View } from 'react-native';
import { useAuth } from '../../context/AuthContext';
import { APP_CONFIG } from '../../config/app';
import { colors, spacing } from '../../theme';
import { AppText } from '../../components/AppText';
import { Button } from '../../components/Button';
import { Card } from '../../components/Card';
import { FormField } from '../../components/FormField';
import { Screen } from '../../components/Screen';

export function LoginScreen() {
  const { signIn } = useAuth();
  const [email, setEmail] = useState('worker@demo.com');
  const [password, setPassword] = useState('123456');
  const [loading, setLoading] = useState(false);

  const handleLogin = async () => {
    try {
      setLoading(true);
      await signIn({ email, password });
    } catch (error) {
      Alert.alert('Acceso denegado', error instanceof Error ? error.message : 'No se pudo iniciar sesión.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Screen>
      <View style={styles.hero}>
        <AppText variant="title">Gestor de Inventario con QR dinámico</AppText>
        <AppText>
          Frontend móvil con doble rol: trabajador para proyectar credencial dinámica y administrador para escanear accesos y préstamos.
        </AppText>
      </View>

      <Card>
        <AppText variant="subtitle">Acceso seguro</AppText>
        <FormField
          label="Correo"
          value={email}
          onChangeText={setEmail}
          autoCapitalize="none"
          keyboardType="email-address"
          placeholder="usuario@empresa.com"
        />
        <FormField
          label="Contraseña"
          value={password}
          onChangeText={setPassword}
          secureTextEntry
          autoCapitalize="none"
          placeholder="••••••"
        />
        <Button label="Entrar" onPress={handleLogin} loading={loading} />
      </Card>

      <Card>
        <AppText variant="subtitle">Credenciales demo</AppText>
        <AppText>Trabajador: worker@demo.com / 123456</AppText>
        <AppText>Administrador: admin@demo.com / 123456</AppText>
        <View style={styles.row}>
          <Button label="Usar trabajador" variant="secondary" onPress={() => { setEmail('worker@demo.com'); setPassword('123456'); }} />
          <Button label="Usar admin" variant="secondary" onPress={() => { setEmail('admin@demo.com'); setPassword('123456'); }} />
        </View>
      </Card>

      <Card>
        <AppText variant="subtitle">Modo de integración</AppText>
        <AppText>{APP_CONFIG.useMockApi ? 'Activo: API mock para demo.' : `Activo: backend real en ${APP_CONFIG.apiBaseUrl}`}</AppText>
        <AppText style={{ color: colors.textMuted }}>
          En web usa automáticamente el host actual con el backend Java en el puerto 8080. En Android Emulator usa 10.0.2.2.
        </AppText>
      </Card>
    </Screen>
  );
}

const styles = StyleSheet.create({
  hero: {
    gap: spacing.sm,
    marginTop: spacing.lg,
    marginBottom: spacing.sm,
  },
  row: {
    flexDirection: 'row',
    gap: spacing.sm,
  },
});
