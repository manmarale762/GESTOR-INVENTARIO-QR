import React, { createContext, PropsWithChildren, useContext, useEffect, useMemo, useState } from 'react';
import { api } from '../services/api';
import { clearSession, getSession, saveSession } from '../services/storage';
import { LoginPayload, Session } from '../types';

interface AuthContextValue {
  session: Session | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  signIn: (payload: LoginPayload) => Promise<void>;
  signOut: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: PropsWithChildren) {
  const [session, setSession] = useState<Session | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let mounted = true;

    (async () => {
      try {
        const storedSession = await getSession();
        if (mounted && storedSession) {
          setSession(storedSession);
        }
      } catch (error) {
        console.warn('No se pudo recuperar la sesión:', error);
      } finally {
        if (mounted) {
          setIsLoading(false);
        }
      }
    })();

    return () => {
      mounted = false;
    };
  }, []);

  const value = useMemo<AuthContextValue>(() => ({
    session,
    isAuthenticated: Boolean(session),
    isLoading,
    signIn: async (payload) => {
      const nextSession = await api.login(payload);
      setSession(nextSession);
      await saveSession(nextSession);
    },
    signOut: async () => {
      setSession(null);
      await clearSession();
    },
  }), [isLoading, session]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe usarse dentro de AuthProvider');
  }
  return context;
}
