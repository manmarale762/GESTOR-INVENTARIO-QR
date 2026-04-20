import axios from 'axios';
import { APP_CONFIG } from '../config/app';
import {
  buildSession,
  dashboardMetrics,
  demoPassword,
  inventory,
  loansByUser,
  movementHistory,
  users,
} from '../data/mock';
import {
  CatalogOption,
  DashboardMetrics,
  InventoryItem,
  Loan,
  LoginPayload,
  ScanRecord,
  ScanValidationRequest,
  ScanValidationResponse,
  Session,
} from '../types';
import { verifyDynamicQr } from './qr';

const client = axios.create({
  baseURL: APP_CONFIG.apiBaseUrl,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

function wait(ms = 350) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function toApiError(error: unknown): Error {
  if (axios.isAxiosError(error)) {
    const payload = error.response?.data;
    if (typeof payload === 'string' && payload.trim()) {
      return new Error(payload);
    }
    if (payload && typeof payload === 'object' && 'message' in payload && typeof payload.message === 'string') {
      return new Error(payload.message);
    }
    return new Error(`Error HTTP ${error.response?.status ?? 'desconocido'} al llamar al backend.`);
  }
  return error instanceof Error ? error : new Error('Se produjo un error inesperado.');
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

async function getAccessTargetsMock(): Promise<CatalogOption[]> {
  await wait();

  const values = Array.from(
    new Set([
      APP_CONFIG.defaultZone,
      'Jaula de seguridad',
      'Laboratorio técnico',
      'Zona de picking',
      'Zona restringida A',
      'Taller 2',
    ]),
  );

  return values.map((value) => ({ value, label: value }));
}

async function getAssetTargetsMock(): Promise<CatalogOption[]> {
  await wait();

  return inventory.map((item) => ({
    value: item.id,
    label: `${item.name} · ${item.serialNumber}`,
  }));
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

    try {
      const { data } = await client.post<Session>('/auth/login', payload);
      return data;
    } catch (error) {
      throw toApiError(error);
    }
  },

  async getLoans(userId: string) {
    if (APP_CONFIG.useMockApi) {
      return getLoansMock(userId);
    }

    try {
      const { data } = await client.get<Loan[]>(`/workers/${userId}/loans`);
      return data;
    } catch (error) {
      throw toApiError(error);
    }
  },

  async getInventory() {
    if (APP_CONFIG.useMockApi) {
      return getInventoryMock();
    }

    try {
      const { data } = await client.get<InventoryItem[]>('/inventory');
      return data;
    } catch (error) {
      throw toApiError(error);
    }
  },

  async getHistory() {
    if (APP_CONFIG.useMockApi) {
      return getHistoryMock();
    }

    try {
      const { data } = await client.get<ScanRecord[]>('/movements');
      return data;
    } catch (error) {
      throw toApiError(error);
    }
  },

  async getDashboardMetrics() {
    if (APP_CONFIG.useMockApi) {
      return getDashboardMetricsMock();
    }

    try {
      const { data } = await client.get<DashboardMetrics>('/dashboard');
      return data;
    } catch (error) {
      throw toApiError(error);
    }
  },

  async getAccessTargets() {
    if (APP_CONFIG.useMockApi) {
      return getAccessTargetsMock();
    }

    try {
      const { data } = await client.get<CatalogOption[]>('/catalog/access-targets');
      return data;
    } catch (error) {
      throw toApiError(error);
    }
  },

  async getAssetTargets() {
    if (APP_CONFIG.useMockApi) {
      return getAssetTargetsMock();
    }

    try {
      const { data } = await client.get<CatalogOption[]>('/catalog/asset-targets');
      return data;
    } catch (error) {
      throw toApiError(error);
    }
  },

  async validateScan(payload: ScanValidationRequest) {
    if (APP_CONFIG.useMockApi) {
      return validateScanMock(payload);
    }

    try {
      const { data } = await client.post<ScanValidationResponse>('/access/validate', payload);
      return data;
    } catch (error) {
      throw toApiError(error);
    }
  },
};