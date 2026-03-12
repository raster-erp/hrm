export interface LeaveTrendEntry {
  year: number;
  month: number;
  leaveTypeName: string;
  applicationCount: number;
  totalDays: number;
}

export interface LeaveTrendReport {
  startYear: number;
  startMonth: number;
  endYear: number;
  endMonth: number;
  departmentId: number | null;
  departmentName: string | null;
  entries: LeaveTrendEntry[];
}

export interface AbsenteeismRateEntry {
  departmentId: number;
  departmentName: string;
  employeeCount: number;
  totalLeaveDays: number;
  totalWorkingDays: number;
  absenteeismRate: number;
}

export interface AbsenteeismRateReport {
  startDate: string;
  endDate: string;
  overallRate: number;
  entries: AbsenteeismRateEntry[];
}

export interface LeaveUtilizationEntry {
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  departmentName: string | null;
  leaveTypeName: string;
  entitled: number;
  used: number;
  available: number;
  utilizationPercent: number;
}

export interface LeaveUtilizationReport {
  year: number;
  departmentId: number | null;
  departmentName: string | null;
  overallUtilization: number;
  entries: LeaveUtilizationEntry[];
}
