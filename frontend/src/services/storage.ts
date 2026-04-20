import * as SecureStore from 'expo-secure-store';
import { Session } from '../types';

const SESSION_KEY = 'inventory-qr-session';

function canUseLocalStorage() {
  return typeof window !== 'undefined' && typeof window.localStorage !== 'undefined';
}

function canUseSecureStore() {
  return (
    typeof SecureStore !== 'undefined' &&
    typeof SecureStore.getItemAsync === 'function' &&
    typeof SecureStore.setItemAsync === 'function' &&
    typeof SecureStore.deleteItemAsync === 'function'
  );
}

export async function saveSession(session: Session) {
  const raw = JSON.stringify(session);

  if (canUseLocalStorage()) {
    window.localStorage.setItem(SESSION_KEY, raw);
    return;
  }

  if (canUseSecureStore()) {
    await SecureStore.setItemAsync(SESSION_KEY, raw);
  }
}

export async function getSession(): Promise<Session | null> {
  try {
    if (canUseLocalStorage()) {
      const raw = window.localStorage.getItem(SESSION_KEY);
      return raw ? (JSON.parse(raw) as Session) : null;
    }

    if (canUseSecureStore()) {
      const raw = await SecureStore.getItemAsync(SESSION_KEY);
      return raw ? (JSON.parse(raw) as Session) : null;
    }

    return null;
  } catch (error) {
    console.warn('Error recuperando la sesión:', error);
    return null;
  }
}

export async function clearSession() {
  if (canUseLocalStorage()) {
    window.localStorage.removeItem(SESSION_KEY);
    return;
  }

  if (canUseSecureStore()) {
    await SecureStore.deleteItemAsync(SESSION_KEY);
  }
}
