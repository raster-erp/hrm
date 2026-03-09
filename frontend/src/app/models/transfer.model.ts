export interface TransferRequest {
  employeeId: number;
  fromDepartmentId: number;
  toDepartmentId: number;
  fromBranch?: string;
  toBranch?: string;
  transferType: string;
  effectiveDate: string;
  reason?: string;
}

export interface TransferResponse {
  id: number;
  employeeId: number;
  employeeName: string;
  fromDepartmentId: number;
  fromDepartmentName: string;
  toDepartmentId: number;
  toDepartmentName: string;
  fromBranch?: string;
  toBranch?: string;
  transferType: string;
  effectiveDate: string;
  reason?: string;
  status: string;
  approvedById?: number;
  approvedByName?: string;
  approvedAt?: string;
  createdAt: string;
  updatedAt: string;
}
