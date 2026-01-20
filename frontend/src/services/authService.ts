import api from './api';
import { LoginRequest, LoginResponse, UserRole } from '../types';

const AUTH_ENDPOINT = '/api/auth/login';

export const authService = {
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await api.post<LoginResponse>(AUTH_ENDPOINT, credentials);
    return response.data;
  },

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('username');
    localStorage.removeItem('userId');
  },

  getToken(): string | null {
    return localStorage.getItem('token');
  },

  getRole(): UserRole | null {
    const role = localStorage.getItem('role');
    return role as UserRole | null;
  },

  getUsername(): string | null {
    return localStorage.getItem('username');
  },

  isAuthenticated(): boolean {
    return !!this.getToken();
  },

  saveAuthData(token: string, role: UserRole, username?: string, userId?: string): void {
    localStorage.setItem('token', token);
    localStorage.setItem('role', role);
    if (username) {
      localStorage.setItem('username', username);
    }
    if (userId) {
      localStorage.setItem('userId', userId);
    }
  },
};

