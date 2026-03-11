export interface AttendanceDeviationRequest {
  employeeId: number;
  deviationDate: string;
  type: string;
  deviationMinutes: number;
  scheduledTime: string;
  actualTime: string;
  gracePeriodMinutes: number | null;
  penaltyAction: string | null;
  remarks: string;
}

export interface AttendanceDeviationResponse {
  id: number;
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  deviationDate: string;
  type: string;
  deviationMinutes: number;
  scheduledTime: string;
  actualTime: string;
  gracePeriodMinutes: number;
  penaltyAction: string;
  status: string;
  remarks: string;
  approvedBy: string | null;
  approvedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface DeviationApprovalRequest {
  status: string;
  approvedBy: string;
  remarks: string;
}

export interface DeviationSummaryResponse {
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  lateComingCount: number;
  earlyGoingCount: number;
  totalDeviationMinutes: number;
  lateComingMinutes: number;
  earlyGoingMinutes: number;
  warningCount: number;
  leaveDeductionCount: number;
  payCutCount: number;
}
