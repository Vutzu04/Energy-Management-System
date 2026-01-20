export type UserRole = 'Administrator' | 'Client';

export interface User {
  id: string;
  username: string;
  password?: string;
  role?: UserRole;
}

export interface Device {
  id: string;
  name: string;
  maximumConsumptionValue: number;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  role: UserRole;
  username?: string;
  userId?: string;
}

export interface AuthState {
  token: string | null;
  role: UserRole | null;
  username: string | null;
  isAuthenticated: boolean;
}

