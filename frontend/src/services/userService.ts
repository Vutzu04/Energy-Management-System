import api from './api';
import { User, UserRole } from '../types';

const USER_ENDPOINT = '/api/users';

export interface UserCreateRequest {
  username: string;
  password: string;
  role?: UserRole;
}

export const userService = {
  async getAllUsers(): Promise<User[]> {
    const response = await api.get<User[]>(USER_ENDPOINT);
    return response.data;
  },

  async getUserById(id: string): Promise<User> {
    const response = await api.get<User>(`${USER_ENDPOINT}/${id}`);
    return response.data;
  },

  async createUser(user: UserCreateRequest): Promise<User> {
    // Use new admin-create endpoint that handles both databases
    const response = await api.post<User>(`${USER_ENDPOINT}/admin-create`, user);
    return response.data;
  },

  async updateUser(id: string, user: Partial<User>): Promise<User> {
    const response = await api.put<User>(`${USER_ENDPOINT}/${id}`, user);
    return response.data;
  },

  async deleteUser(id: string): Promise<void> {
    await api.delete(`${USER_ENDPOINT}/${id}`);
  },
};

