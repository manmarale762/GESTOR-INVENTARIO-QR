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
}

export interface Session {
  token: string;
  user: User;
}

export interface LoginPayload {
  email: string;
  password: string;
}
}

export interface ScanValidationResponse {
  approved: boolean;
  message: string;
  record: ScanRecord;
}
