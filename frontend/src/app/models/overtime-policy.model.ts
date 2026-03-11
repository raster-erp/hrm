export interface OvertimePolicyRequest {
  name: string;
  type: string;
  rateMultiplier: number;
  minOvertimeMinutes: number | null;
  maxOvertimeMinutesPerDay: number | null;
  maxOvertimeMinutesPerMonth: number | null;
  requiresApproval: boolean;
  description: string;
}

export interface OvertimePolicyResponse {
  id: number;
  name: string;
  type: string;
  rateMultiplier: number;
  minOvertimeMinutes: number;
  maxOvertimeMinutesPerDay: number | null;
  maxOvertimeMinutesPerMonth: number | null;
  requiresApproval: boolean;
  active: boolean;
  description: string;
  createdAt: string;
  updatedAt: string;
}
