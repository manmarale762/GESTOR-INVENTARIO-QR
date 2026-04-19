import { DashboardMetrics, InventoryItem, Loan, ScanRecord, Session, User } from '../types';

export const users: User[] = [
  {
    id: 'u-worker-01',
    fullName: 'Laura Martín',
    email: 'worker@demo.com',
    role: 'worker',
    employeeCode: 'EMP-2048',
    zoneName: 'Almacén Norte',
    department: 'Operaciones',
    avatarColor: '#2563eb',
    totpSecret: 'JBSWY3DPEHPK3PXP',
  },
  {
    id: 'u-admin-01',
    fullName: 'Carlos Ortega',
    email: 'admin@demo.com',
    role: 'admin',
    employeeCode: 'ADM-0099',
    zoneName: 'Control Central',
    department: 'Seguridad e Inventario',
    avatarColor: '#16a34a',
    totpSecret: 'KRUGS4ZANFZSAYJA',
  },
];

export const loansByUser: Record<string, Loan[]> = {
  'u-worker-01': [
    {
      id: 'loan-01',
      itemId: 'item-01',
      itemName: 'Escáner térmico FLIR',
      serialNumber: 'FL-8891',
      borrowedAt: '2026-04-08T08:00:00Z',
      dueAt: '2026-04-10T18:00:00Z',
      status: 'active',
      location: 'Laboratorio técnico',
    },
    {
      id: 'loan-02',
      itemId: 'item-02',
      itemName: 'Tableta industrial Zebra',
      serialNumber: 'ZB-1022',
      borrowedAt: '2026-04-07T09:30:00Z',
      dueAt: '2026-04-09T16:30:00Z',
      status: 'overdue',
      location: 'Zona de picking',
    },
  ],
  'u-admin-01': [],
};

export const inventory: InventoryItem[] = [
  {
    id: 'item-01',
    name: 'Escáner térmico FLIR',
    serialNumber: 'FL-8891',
    category: 'Herramienta de diagnóstico',
    location: 'Laboratorio técnico',
    quantity: 1,
    highValue: true,
    assignedTo: 'Laura Martín',
    status: 'loaned',
  },
  {
    id: 'item-02',
    name: 'Tableta industrial Zebra',
    serialNumber: 'ZB-1022',
    category: 'Dispositivo móvil',
    location: 'Zona de picking',
    quantity: 1,
    highValue: true,
    assignedTo: 'Laura Martín',
    status: 'loaned',
  },
  {
    id: 'item-03',
    name: 'Llave dinamométrica Bosch',
    serialNumber: 'BS-2214',
    category: 'Herramienta',
    location: 'Jaula de seguridad',
    quantity: 1,
    highValue: true,
    status: 'available',
  },
  {
    id: 'item-04',
    name: 'Kit de calibración Omega',
    serialNumber: 'OM-3011',
    category: 'Instrumentación',
    location: 'Zona restringida A',
    quantity: 1,
    highValue: true,
    status: 'restricted',
  },
  {
    id: 'item-05',
    name: 'Soldador portátil Weller',
    serialNumber: 'WL-9014',
    category: 'Herramienta',
    location: 'Taller 2',
    quantity: 1,
    highValue: false,
    status: 'maintenance',
  },
];

export const movementHistory: ScanRecord[] = [
  {
    id: 'scan-01',
    workerName: 'Laura Martín',
    workerId: 'u-worker-01',
    action: 'access',
    target: 'Zona restringida A',
    result: 'approved',
    timestamp: '2026-04-09T07:59:10Z',
  },
  {
    id: 'scan-02',
    workerName: 'Laura Martín',
    workerId: 'u-worker-01',
    action: 'checkout',
    target: 'Escáner térmico FLIR',
    result: 'approved',
    timestamp: '2026-04-08T08:01:03Z',
  },
  {
    id: 'scan-03',
    workerName: 'Laura Martín',
    workerId: 'u-worker-01',
    action: 'checkout',
    target: 'Tableta industrial Zebra',
    result: 'approved',
    timestamp: '2026-04-07T09:31:24Z',
  },
];

export const dashboardMetrics: DashboardMetrics = {
  activeLoans: inventory.filter((item) => item.status === 'loaned').length,
  restrictedZones: 3,
  availableAssets: inventory.filter((item) => item.status === 'available').length,
  todayScans: 16,
};

export const demoPassword = '123456';

export function buildSession(user: User): Session {
  return {
    token: `mock-token-${user.id}`,
    refreshToken: `mock-refresh-${user.id}`,
    user,
  };
}
