import api from './api';
import { Device } from '../types';

const DEVICE_ENDPOINT = '/api/devices';

export const deviceService = {
  async getAllDevices(): Promise<Device[]> {
    const response = await api.get<Device[]>(DEVICE_ENDPOINT);
    return response.data;
  },

  async getDeviceById(id: string): Promise<Device> {
    const response = await api.get<Device>(`${DEVICE_ENDPOINT}/${id}`);
    return response.data;
  },

  async getMyDevices(): Promise<Device[]> {
    const response = await api.get<Device[]>(`${DEVICE_ENDPOINT}/my-devices`);
    return response.data;
  },

  async createDevice(device: Omit<Device, 'id'>): Promise<Device> {
    const response = await api.post<Device>(DEVICE_ENDPOINT, device);
    return response.data;
  },

  async updateDevice(id: string, device: Partial<Device>): Promise<Device> {
    const response = await api.put<Device>(`${DEVICE_ENDPOINT}/${id}`, device);
    return response.data;
  },

  async deleteDevice(id: string): Promise<void> {
    await api.delete(`${DEVICE_ENDPOINT}/${id}`);
  },
};

