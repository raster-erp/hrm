export interface LeaveTypeRequest {
  code: string;
  name: string;
  category: string;
  description: string;
}

export interface LeaveTypeResponse {
  id: number;
  code: string;
  name: string;
  category: string;
  description: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}
