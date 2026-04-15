import { Session, User } from '../types';

export const demoPassword = 'demo123';

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
  },
];

export function buildSession(user: User): Session {
  return {
    token: `token-${user.id}`,
    user,
  };
}
