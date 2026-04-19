import CryptoJS from 'crypto-js';

const BASE32_ALPHABET = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ234567';

function normalizeSecret(secret: string) {
  return secret.replace(/=+$/g, '').replace(/\s+/g, '').toUpperCase();
}

export function base32ToBytes(secret: string): number[] {
  const normalized = normalizeSecret(secret);
  let bits = '';

  for (const char of normalized) {
    const index = BASE32_ALPHABET.indexOf(char);
    if (index === -1) {
      throw new Error(`Carácter Base32 no válido: ${char}`);
    }
    bits += index.toString(2).padStart(5, '0');
  }

  const bytes: number[] = [];
  for (let i = 0; i + 8 <= bits.length; i += 8) {
    bytes.push(parseInt(bits.slice(i, i + 8), 2));
  }

  return bytes;
}

function bytesToHex(bytes: number[]): string {
  return bytes.map((byte) => byte.toString(16).padStart(2, '0')).join('');
}

function hexToBytes(hex: string): number[] {
  const bytes: number[] = [];
  for (let i = 0; i < hex.length; i += 2) {
    bytes.push(parseInt(hex.slice(i, i + 2), 16));
  }
  return bytes;
}

export function generateTOTP(secret: string, epochSeconds: number, period = 10, digits = 6): string {
  const counter = BigInt(Math.floor(epochSeconds / period));
  const counterHex = counter.toString(16).padStart(16, '0');

  const key = CryptoJS.enc.Hex.parse(bytesToHex(base32ToBytes(secret)));
  const message = CryptoJS.enc.Hex.parse(counterHex);
  const hashHex = CryptoJS.HmacSHA1(message, key).toString(CryptoJS.enc.Hex);
  const hashBytes = hexToBytes(hashHex);

  const offset = hashBytes[hashBytes.length - 1] & 0x0f;
  const binary =
    ((hashBytes[offset] & 0x7f) << 24) |
    ((hashBytes[offset + 1] & 0xff) << 16) |
    ((hashBytes[offset + 2] & 0xff) << 8) |
    (hashBytes[offset + 3] & 0xff);

  const otp = binary % 10 ** digits;
  return otp.toString().padStart(digits, '0');
}

export function getSecondsRemaining(epochSeconds: number, period = 10): number {
  const remainder = Math.floor(epochSeconds) % period;
  return remainder === 0 ? period : period - remainder;
}
