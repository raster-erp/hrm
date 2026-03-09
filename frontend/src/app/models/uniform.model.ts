export interface UniformRequest {
  name: string;
  type: string;
  size: string;
  description?: string;
}

export interface UniformResponse {
  id: number;
  name: string;
  type: string;
  size: string;
  description?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UniformAllocationRequest {
  employeeId: number;
  uniformId: number;
  allocatedDate: string;
  quantity: number;
}

export interface UniformAllocationResponse {
  id: number;
  employeeId: number;
  employeeName: string;
  uniformId: number;
  uniformName: string;
  allocatedDate: string;
  returnedDate?: string;
  quantity: number;
  status: string;
  createdAt: string;
}
