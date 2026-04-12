import React from 'react';
import { Text } from 'react-native';
import { useAuth } from '../../context/AuthContext';
import { Screen } from '../../components/Screen';
import { AppText } from '../../components/AppText';

export function WorkerHomeScreen() {
  const { session } = useAuth();

  return (
    <Screen>
      <AppText >Hola, {session?.user.fullName ?? 'Usuario'}</AppText>
      <Text>Próximamente: Pantalla principal del trabajador</Text>
    </Screen>
  );
}
