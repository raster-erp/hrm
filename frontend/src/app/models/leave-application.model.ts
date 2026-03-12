export interface LeaveApplicationRequest {
  employeeId: number;
  leaveTypeId: number;
  fromDate: string;
  toDate: string;
  numberOfDays: number;
  reason: string | null;
  remarks: string | null;
}

export interface LeaveApplicationResponse {
  id: number;
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  leaveTypeId: number;
  leaveTypeName: string;
  fromDate: string;
  toDate: string;
  numberOfDays: number;
  reason: string;
  status: string;
  approvalLevel: number;
  remarks: string | null;
  approvedBy: string | null;
  approvedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface LeaveApprovalRequest {
  status: string;
  approvedBy: string;
  remarks: string;
}
