import axios from 'axios';
import { APP_CONFIG } from '../config/app';
import { buildSession, demoPassword, users } from '../data/mock';
import { LoginPayload, Session } from '../types';

const client = axios.create({
  baseURL: APP_CONFIG.apiBaseUrl,
  timeout: 7000,
});

function wait(ms = 350) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function loginMock(payload: LoginPayload): Promise<Session> {
  await wait();
  const user = users.find((entry) => entry.email.toLowerCase() === payload.email.trim().toLowerCase());
  if (!user || payload.password !== demoPassword) {
    throw new Error('Credenciales no válidas. Usa las credenciales demo indicadas en la pantalla de acceso.');
  }
  return buildSession(user);
}

export const api = {
  async login(payload: LoginPayload) {
    if (APP_CONFIG.useMockApi) {
      return loginMock(payload);
    }

    const { data } = await client.post<Session>('/auth/login', payload);
    return data;
  },
};
