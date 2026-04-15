import './global.css';
import { useAuth } from '../context/AuthContext';
import { Redirect, Slot } from 'expo-router';
import { ActivityIndicator, View } from 'react-native';
import { colors } from '../theme';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { AuthProvider } from '../context/AuthContext';

export default function RootLayout() {
  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <AuthProvider>
          <StatusBar style="light" />
          <AuthCheck />
        </AuthProvider>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}

function AuthCheck() {
  const { isAuthenticated, session, isLoading } = useAuth();

  if (isLoading) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: colors.background }}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  if (!isAuthenticated) {
    return <Slot />;
  }

  if (session?.user.role === 'admin') {
    return <Redirect href="/(admin)" />;
  }

  return <Redirect href="/(worker)" />;
}