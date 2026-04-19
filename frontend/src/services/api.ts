import axios from 'axios';
import { APP_CONFIG } from '../config/app';
import { buildSession, dashboardMetrics, demoPassword, inventory, loansByUser, movementHistory, users } from '../data/mock';
import { DashboardMetrics, InventoryItem, Loan, LoginPayload, ScanRecord, ScanValidationRequest, ScanValidationResponse, Session } from '../types';
import { verifyDynamicQr } from './qr';

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

async function getLoansMock(userId: string): Promise<Loan[]> {
  await wait();
  return loansByUser[userId] ?? [];
}

async function getInventoryMock(): Promise<InventoryItem[]> {
  await wait();
  return inventory;
}

async function getHistoryMock(): Promise<ScanRecord[]> {
  await wait();
  return [...movementHistory].sort((a, b) => +new Date(b.timestamp) - +new Date(a.timestamp));
}

async function getDashboardMetricsMock(): Promise<DashboardMetrics> {
  await wait();
  return dashboardMetrics;
}

async function validateScanMock(request: ScanValidationRequest): Promise<ScanValidationResponse> {
  await wait();
  const validation = verifyDynamicQr(request.qrContent);
  const worker = users.find((entry) => entry.id === validation.workerId);

  const approved = validation.approved;
  const record: ScanRecord = {
    id: `scan-${Date.now()}`,
    workerName: worker?.fullName ?? 'Desconocido',
    workerId: worker?.id ?? 'unknown',
    action: request.action,
    target: request.target,
    result: approved ? 'approved' : 'denied',
    timestamp: new Date().toISOString(),
    reason: approved ? undefined : validation.message,
  };

  movementHistory.unshift(record);

  return {
    approved,
    message: approved
      ? `${worker?.fullName ?? 'Trabajador'} autorizado para ${labelAction(request.action)}.`
      : validation.message,
    record,
  };
}

function labelAction(action: ScanValidationRequest['action']) {
  switch (action) {
    case 'access':
      return 'acceder';
    case 'checkout':
      return 'retirar material';
    case 'return':
      return 'devolver material';
    default:
      return 'operar';
  }
}

export const api = {
  async login(payload: LoginPayload) {
    if (APP_CONFIG.useMockApi) {
      return loginMock(payload);
    }

    const { data } = await client.post<Session>('/auth/login', payload);
    return data;
  },

  async getLoans(userId: string) {
    if (APP_CONFIG.useMockApi) {
      return getLoansMock(userId);
    }

    const { data } = await client.get<Loan[]>(`/workers/${userId}/loans`);
    return data;
  },

  async getInventory() {
    if (APP_CONFIG.useMockApi) {
      return getInventoryMock();
    }

    const { data } = await client.get<InventoryItem[]>('/inventory');
    return data;
  },

  async getHistory() {
    if (APP_CONFIG.useMockApi) {
      return getHistoryMock();
    }

    const { data } = await client.get<ScanRecord[]>('/movements');
    return data;
  },

  async getDashboardMetrics() {
    if (APP_CONFIG.useMockApi) {
      return getDashboardMetricsMock();
    }

    const { data } = await client.get<DashboardMetrics>('/dashboard');
    return data;
  },

  async validateScan(payload: ScanValidationRequest) {
    if (APP_CONFIG.useMockApi) {
      return validateScanMock(payload);
    }

    const { data } = await client.post<ScanValidationResponse>('/access/validate', payload);
    return data;
  },
};
