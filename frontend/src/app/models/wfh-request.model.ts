export interface WfhRequestCreateRequest {
  employeeId: number;
  requestDate: string;
  reason: string;
  remarks: string;
}

export interface WfhRequestResponse {
  id: number;
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  requestDate: string;
  reason: string;
  status: string;
  approvedBy: string | null;
  approvedAt: string | null;
  remarks: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface WfhApprovalRequest {
  status: string;
  approvedBy: string;
  remarks: string;
}

export interface WfhCheckInRequest {
  wfhRequestId: number;
  ipAddress: string | null;
  location: string | null;
}

export interface WfhCheckInResponse {
  id: number;
  wfhRequestId: number;
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  checkInTime: string | null;
  checkOutTime: string | null;
  ipAddress: string | null;
  location: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface WfhDashboardResponse {
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  totalRequests: number;
  approvedRequests: number;
  pendingRequests: number;
  rejectedRequests: number;
  checkedInToday: boolean;
}
