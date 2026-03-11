export interface LeavePolicyRequest {
  name: string;
  leaveTypeId: number;
  accrualFrequency: string;
  accrualDays: number;
  maxAccumulation: number | null;
  carryForwardLimit: number | null;
  proRataForNewJoiners: boolean;
  minServiceDaysRequired: number | null;
  description: string;
}

export interface LeavePolicyResponse {
  id: number;
  name: string;
  leaveTypeId: number;
  leaveTypeName: string;
  leaveTypeCode: string;
  accrualFrequency: string;
  accrualDays: number;
  maxAccumulation: number | null;
  carryForwardLimit: number | null;
  proRataForNewJoiners: boolean;
  minServiceDaysRequired: number;
  active: boolean;
  description: string;
  createdAt: string;
  updatedAt: string;
}
