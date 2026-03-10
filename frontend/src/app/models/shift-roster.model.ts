export interface ShiftRosterRequest {
  employeeId: number;
  shiftId: number;
  effectiveDate: string;
  endDate: string | null;
  rotationPatternId: number | null;
}

export interface BulkShiftRosterRequest {
  employeeIds: number[];
  shiftId: number;
  effectiveDate: string;
  endDate: string | null;
  rotationPatternId: number | null;
}

export interface ShiftRosterResponse {
  id: number;
  employeeId: number;
  employeeName: string;
  employeeCode: string;
  shiftId: number;
  shiftName: string;
  effectiveDate: string;
  endDate: string;
  rotationPatternId: number | null;
  rotationPatternName: string | null;
  createdAt: string;
  updatedAt: string;
}
