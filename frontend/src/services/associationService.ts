import api from './api';

const ASSOCIATION_ENDPOINT = '/api/associations';

export interface AssociationRequest {
  userId: string;
  deviceId: string;
}

export interface AssociationResponse {
  userId: string;
  deviceId: string;
  deviceName: string;
  maximumConsumptionValue: number;
}

export const associationService = {
  async getAllAssociations(): Promise<AssociationResponse[]> {
    const response = await api.get<AssociationResponse[]>(ASSOCIATION_ENDPOINT);
    return response.data;
  },

  async associateDeviceToUser(association: AssociationRequest): Promise<void> {
    await api.post(ASSOCIATION_ENDPOINT, association);
  },

  async removeAssociation(userId: string, deviceId: string): Promise<void> {
    await api.delete(`${ASSOCIATION_ENDPOINT}/${userId}/${deviceId}`);
  },
};

