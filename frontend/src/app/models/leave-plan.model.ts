export type LeavePlanStatus = 'PLANNED' | 'CANCELLED' | 'CONVERTED';

export interface LeavePlanRequest {
  employeeId: number;
  leaveTypeId: number;
  plannedFromDate: string;
  plannedToDate: string;
  numberOfDays: number;
  notes?: string;
}

export interface LeavePlanResponse {
  id: number;
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  leaveTypeId: number;
  leaveTypeName: string;
  plannedFromDate: string;
  plannedToDate: string;
  numberOfDays: number;
  notes: string | null;
  status: LeavePlanStatus;
  createdAt: string;
  updatedAt: string;
}
