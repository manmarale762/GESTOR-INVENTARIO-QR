import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { Ionicons } from '@expo/vector-icons';
import { AdminDashboardScreen } from '../screens/admin/AdminDashboardScreen';
import { InventoryScreen } from '../screens/admin/InventoryScreen';
import { MovementHistoryScreen } from '../screens/admin/MovementHistoryScreen';
import { ScannerScreen } from '../screens/admin/ScannerScreen';
import { ProfileScreen } from '../screens/common/ProfileScreen';
import { colors } from '../theme';

const Tab = createBottomTabNavigator();

export function AdminTabs() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarStyle: { backgroundColor: colors.surface, borderTopColor: colors.border },
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.textMuted,
        tabBarIcon: ({ color, size }) => {
          const iconMap: Record<string, keyof typeof Ionicons.glyphMap> = {
            Panel: 'grid-outline',
            Escaner: 'scan-outline',
            Inventario: 'cube-outline',
            Historial: 'time-outline',
            Perfil: 'person-outline',
          };
          return <Ionicons name={iconMap[route.name]} size={size} color={color} />;
        },
      })}
    >
      <Tab.Screen name="Panel" component={AdminDashboardScreen} />
      <Tab.Screen name="Escaner" component={ScannerScreen} />
      <Tab.Screen name="Inventario" component={InventoryScreen} />
      <Tab.Screen name="Historial" component={MovementHistoryScreen} />
      <Tab.Screen name="Perfil" component={ProfileScreen} />
    </Tab.Navigator>
  );
}
