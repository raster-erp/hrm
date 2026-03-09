export interface SeparationRequest {
  employeeId: number;
  separationType: string;
  reason: string;
  noticeDate: string;
  lastWorkingDay: string;
}

export interface SeparationResponse {
  id: number;
  employeeId: number;
  employeeName: string;
  separationType: string;
  reason: string;
  noticeDate: string;
  lastWorkingDay: string;
  status: string;
  approvedById?: number;
  approvedByName?: string;
  approvedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ExitChecklistRequest {
  separationId: number;
  itemName: string;
  department: string;
  notes?: string;
}

export interface ExitChecklistResponse {
  id: number;
  separationId: number;
  itemName: string;
  department: string;
  notes?: string;
  cleared: boolean;
  clearedBy?: string;
  clearedAt?: string;
  createdAt: string;
}

export interface NoDuesRequest {
  separationId: number;
  department: string;
  amountDue: number;
  notes?: string;
}

export interface NoDuesResponse {
  id: number;
  separationId: number;
  department: string;
  amountDue: number;
  notes?: string;
  cleared: boolean;
  clearedBy?: string;
  clearedAt?: string;
  createdAt: string;
}
