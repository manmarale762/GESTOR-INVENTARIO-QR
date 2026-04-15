import { APP_CONFIG } from '../config/app';
import { users } from '../data/mock';
import { DynamicQrPayload, User } from '../types';
import { generateTOTP } from './totp';

export function buildDynamicQrPayload(user: User, now = Date.now()): DynamicQrPayload {
  const periodMs = APP_CONFIG.qrRefreshIntervalSeconds * 1000;
  const issuedAt = Math.floor(now / periodMs) * periodMs;
  const expiresAt = issuedAt + periodMs;
  const token = generateTOTP(user.totpSecret, Math.floor(issuedAt / 1000), APP_CONFIG.qrRefreshIntervalSeconds, 6);

  return {
    version: '1.0',
    workerId: user.id,
    employeeCode: user.employeeCode,
    token,
    issuedAt,
    expiresAt,
    nonce: `${user.employeeCode}-${Math.floor(issuedAt / 1000)}`,
  };
}

export function encodeDynamicQr(user: User, now = Date.now()): string {
  return JSON.stringify(buildDynamicQrPayload(user, now));
}

export function parseDynamicQr(raw: string): DynamicQrPayload | null {
  try {
    const parsed = JSON.parse(raw) as DynamicQrPayload;
    if (!parsed.workerId || !parsed.token || !parsed.issuedAt || !parsed.expiresAt) {
      return null;
    }
    return parsed;
  } catch {
    return null;
  }
}

export function verifyDynamicQr(raw: string, now = Date.now()): { approved: boolean; message: string; workerId?: string } {
  const payload = parseDynamicQr(raw);
  if (!payload) {
    return { approved: false, message: 'QR no válido o formato desconocido.' };
  }

  const user = users.find((entry) => entry.id === payload.workerId);
  if (!user) {
    return { approved: false, message: 'Usuario no reconocido.' };
  }

  if (now > payload.expiresAt + APP_CONFIG.qrGraceWindowSeconds * 1000) {
    return { approved: false, message: 'El código QR ha expirado.' };
  }

  const validSteps = [payload.issuedAt, payload.issuedAt - 1000 * APP_CONFIG.qrRefreshIntervalSeconds, payload.issuedAt + 1000 * APP_CONFIG.qrRefreshIntervalSeconds];
  const isValid = validSteps.some((issuedAt) => {
    const expected = generateTOTP(user.totpSecret, Math.floor(issuedAt / 1000), APP_CONFIG.qrRefreshIntervalSeconds, 6);
    return expected === payload.token;
  });

  if (!isValid) {
    return { approved: false, message: 'Token TOTP inválido.' };
  }

  return { approved: true, message: 'Código QR validado correctamente.', workerId: user.id };
}
