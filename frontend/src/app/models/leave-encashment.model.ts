export interface EncashmentEligibilityResponse {
  employeeId: number;
  employeeName: string;
  leaveTypeId: number;
  leaveTypeName: string;
  year: number;
  eligible: boolean;
  availableBalance: number;
  minRequiredBalance: number;
  maxEncashableDays: number;
  perDaySalary: number;
  reason: string;
}

export interface LeaveEncashmentRequest {
  employeeId: number;
  leaveTypeId: number;
  year: number;
  numberOfDays: number;
  remarks?: string;
}

export interface LeaveEncashmentResponse {
  id: number;
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  leaveTypeId: number;
  leaveTypeName: string;
  year: number;
  numberOfDays: number;
  perDaySalary: number;
  totalAmount: number;
  status: string;
  approvedBy: string | null;
  approvedAt: string | null;
  remarks: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface LeaveEncashmentApprovalRequest {
  status: 'APPROVED' | 'REJECTED';
  approvedBy?: string;
  remarks?: string;
}
