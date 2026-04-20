export type UserRole = 'worker' | 'admin';

export interface User {
  id: string;
  fullName: string;
  email: string;
  role: UserRole;
  employeeCode: string;
  zoneName: string;
  department: string;
  avatarColor: string;
  totpSecret: string;
}

export interface Loan {
  id: string;
  itemId: string;
  itemName: string;
  serialNumber: string;
  borrowedAt: string;
  dueAt: string;
  status: 'active' | 'overdue' | 'returned';
  location: string;
}

export interface InventoryItem {
  id: string;
  name: string;
  serialNumber: string;
  category: string;
  location: string;
  quantity: number;
  assignedTo?: string;
  highValue: boolean;
  status: 'available' | 'loaned' | 'maintenance' | 'restricted';
}

export interface ScanRecord {
  id: string;
  workerName: string;
  workerId: string;
  action: 'access' | 'checkout' | 'return';
  target: string;
  result: 'approved' | 'denied';
  timestamp: string;
  reason?: string;
}

export interface Session {
  token: string;
  refreshToken?: string;
  user: User;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface DashboardMetrics {
  activeLoans: number;
  restrictedZones: number;
  availableAssets: number;
  todayScans: number;
}

export interface DynamicQrPayload {
  version: '1.0';
  workerId: string;
  employeeCode: string;
  token: string;
  issuedAt: number;
  expiresAt: number;
  nonce: string;
}

export interface ScanValidationRequest {
  qrContent: string;
  action: 'access' | 'checkout' | 'return';
  target: string;
}

export interface ScanValidationResponse {
  approved: boolean;
  message: string;
  record: ScanRecord;
}

export interface CatalogOption {
  value: string;
  label: string;
}