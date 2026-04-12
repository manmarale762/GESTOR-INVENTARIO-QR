import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { Ionicons } from '@expo/vector-icons';
import { DynamicBadgeScreen } from '../screens/worker/DynamicBadgeScreen';
import { ActiveLoansScreen } from '../screens/worker/ActiveLoansScreen';
import { WorkerHomeScreen } from '../screens/worker/WorkerHomeScreen';
import { ProfileScreen } from '../screens/common/ProfileScreen';
import { colors } from '../theme';

const Tab = createBottomTabNavigator();

export function WorkerTabs() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarStyle: { backgroundColor: colors.surface, borderTopColor: colors.border },
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.textMuted,
        tabBarIcon: ({ color, size }) => {
          const iconMap: Record<string, keyof typeof Ionicons.glyphMap> = {
            Inicio: 'home-outline',
            Credencial: 'qr-code-outline',
            Prestamos: 'cube-outline',
            Perfil: 'person-outline',
          };
          return <Ionicons name={iconMap[route.name]} size={size} color={color} />;
        },
      })}
    >
      <Tab.Screen name="Inicio" component={WorkerHomeScreen} />
      <Tab.Screen name="Credencial" component={DynamicBadgeScreen} />
      <Tab.Screen name="Prestamos" component={ActiveLoansScreen} />
      <Tab.Screen name="Perfil" component={ProfileScreen} />
    </Tab.Navigator>
  );
}
