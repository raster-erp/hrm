export interface LeaveBalanceResponse {
  id: number;
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  leaveTypeId: number;
  leaveTypeName: string;
  leaveTypeCode: string;
  year: number;
  credited: number;
  used: number;
  pending: number;
  available: number;
  carryForwarded: number;
  createdAt: string;
  updatedAt: string;
}

export interface LeaveTransactionResponse {
  id: number;
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  leaveTypeId: number;
  leaveTypeName: string;
  transactionType: string;
  amount: number;
  balanceAfter: number;
  referenceType: string | null;
  referenceId: number | null;
  description: string | null;
  createdBy: string | null;
  createdAt: string;
}

export interface BalanceAdjustmentRequest {
  employeeId: number;
  leaveTypeId: number;
  year: number;
  amount: number;
  description: string | null;
  adjustedBy: string | null;
}

export interface YearEndProcessingRequest {
  year: number;
  processedBy: string | null;
}

export interface YearEndSummaryResponse {
  processedYear: number;
  nextYear: number;
  employeesProcessed: number;
  balancesCreated: number;
  totalCarryForwarded: number;
  totalLapsed: number;
}
