import { Platform } from 'react-native';

function resolveApiBaseUrl() {
  const envUrl = process.env.EXPO_PUBLIC_API_BASE_URL ?? process.env.EXPO_PUBLIC_API_URL;
  if (envUrl && envUrl.trim()) {
    return envUrl.trim();
  }

  if (Platform.OS === 'android') {
    return 'http://10.0.2.2:8080/api';
  }

  if (Platform.OS === 'web' && typeof window !== 'undefined') {
    return `${window.location.protocol}//${window.location.hostname}:8080/api`;
  }

  return 'http://localhost:8080/api';
}

export const APP_CONFIG = {
  apiBaseUrl: resolveApiBaseUrl(),
  useMockApi: false,
  qrRefreshIntervalSeconds: 10,
  qrGraceWindowSeconds: 5,
  defaultZone: 'Zona restringida A',
};
