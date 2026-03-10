export interface OvertimeRecordRequest {
  employeeId: number;
  overtimeDate: string;
  overtimePolicyId: number;
  overtimeMinutes: number;
  shiftStartTime: string | null;
  shiftEndTime: string | null;
  actualStartTime: string | null;
  actualEndTime: string | null;
  remarks: string;
}

export interface OvertimeRecordResponse {
  id: number;
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  overtimeDate: string;
  overtimePolicyId: number;
  overtimePolicyName: string;
  overtimePolicyType: string;
  overtimeMinutes: number;
  status: string;
  source: string;
  shiftStartTime: string | null;
  shiftEndTime: string | null;
  actualStartTime: string | null;
  actualEndTime: string | null;
  remarks: string;
  approvedBy: string | null;
  approvedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface OvertimeApprovalRequest {
  status: string;
  approvedBy: string;
  remarks: string;
}

export interface OvertimeSummaryResponse {
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  totalOvertimeMinutes: number;
  approvedOvertimeMinutes: number;
  pendingOvertimeMinutes: number;
  rejectedOvertimeMinutes: number;
  weightedOvertimeMinutes: number;
  recordCount: number;
}
