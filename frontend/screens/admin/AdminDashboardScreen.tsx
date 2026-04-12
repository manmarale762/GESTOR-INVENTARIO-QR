import React from 'react';
import { Text } from 'react-native';
import { Screen } from '../../components/Screen';
import { AppText } from '../../components/AppText';

export function AdminDashboardScreen() {
  return (
    <Screen>
      <AppText >Panel de control</AppText>
      <Text>Próximamente: Panel de administración</Text>
    </Screen>
  );
}
