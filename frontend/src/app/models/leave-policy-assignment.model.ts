export interface LeavePolicyAssignmentRequest {
  leavePolicyId: number;
  assignmentType: string;
  departmentId: number | null;
  designationId: number | null;
  employeeId: number | null;
  effectiveFrom: string;
  effectiveTo: string | null;
}

export interface LeavePolicyAssignmentResponse {
  id: number;
  leavePolicyId: number;
  leavePolicyName: string;
  assignmentType: string;
  departmentId: number | null;
  departmentName: string | null;
  designationId: number | null;
  designationTitle: string | null;
  employeeId: number | null;
  employeeName: string | null;
  effectiveFrom: string;
  effectiveTo: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}
