export interface RegularizationRequestRequest {
  employeeId: number;
  requestDate: string;
  type: string;
  reason: string;
  originalPunchIn: string | null;
  originalPunchOut: string | null;
  correctedPunchIn: string;
  correctedPunchOut: string;
  remarks: string | null;
}

export interface RegularizationRequestResponse {
  id: number;
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  requestDate: string;
  type: string;
  reason: string;
  originalPunchIn: string | null;
  originalPunchOut: string | null;
  correctedPunchIn: string;
  correctedPunchOut: string;
  status: string;
  approvalLevel: number;
  remarks: string | null;
  approvedBy: string | null;
  approvedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface RegularizationApprovalRequest {
  status: string;
  approvedBy: string;
  remarks: string;
}
