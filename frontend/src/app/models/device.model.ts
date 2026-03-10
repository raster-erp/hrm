export interface DeviceRequest {
  serialNumber: string;
  name: string;
  type: string;
  location: string;
  ipAddress: string;
}

export interface DeviceResponse {
  id: number;
  serialNumber: string;
  name: string;
  type: string;
  location: string;
  ipAddress: string;
  status: string;
  lastSyncAt: string;
  createdAt: string;
  updatedAt: string;
}
