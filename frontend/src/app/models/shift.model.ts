export interface ShiftRequest {
  name: string;
  type: string;
  startTime: string;
  endTime: string;
  breakDurationMinutes: number | null;
  gracePeriodMinutes: number | null;
  description: string;
}

export interface ShiftResponse {
  id: number;
  name: string;
  type: string;
  startTime: string;
  endTime: string;
  breakDurationMinutes: number;
  gracePeriodMinutes: number;
  description: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}
