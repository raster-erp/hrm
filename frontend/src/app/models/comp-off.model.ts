export interface CompOffCreditRequest {
  employeeId: number;
  workedDate: string;
  reason: string;
  hoursWorked?: number;
  remarks?: string;
}

export interface CompOffCreditResponse {
  id: number;
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  workedDate: string;
  reason: string;
  creditDate: string;
  expiryDate: string;
  hoursWorked: number | null;
  status: string;
  approvedBy: string | null;
  approvedAt: string | null;
  usedDate: string | null;
  remarks: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CompOffApprovalRequest {
  status: 'APPROVED' | 'REJECTED';
  approvedBy?: string;
  remarks?: string;
}

export interface CompOffBalanceResponse {
  employeeId: number;
  employeeName: string;
  totalCredits: number;
  approved: number;
  pending: number;
  used: number;
  expired: number;
  availableForUse: number;
}
