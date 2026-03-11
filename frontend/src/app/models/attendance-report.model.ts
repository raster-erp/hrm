export interface DailyMusterEntry {
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  departmentName: string | null;
  date: string;
  firstPunchIn: string | null;
  lastPunchOut: string | null;
  totalPunches: number;
  status: string;
}

export interface DailyMusterReport {
  date: string;
  departmentId: number | null;
  departmentName: string | null;
  entries: DailyMusterEntry[];
  totalPresent: number;
  totalAbsent: number;
  totalIncomplete: number;
}

export interface MonthlySummaryEntry {
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  departmentName: string | null;
  totalPresent: number;
  totalAbsent: number;
  totalIncomplete: number;
  totalWorkingDays: number;
}

export interface MonthlySummaryReport {
  year: number;
  month: number;
  departmentId: number | null;
  departmentName: string | null;
  entries: MonthlySummaryEntry[];
}

export interface AbsenteeEntry {
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  departmentName: string | null;
  absentDate: string;
}

export interface AbsenteeListReport {
  startDate: string;
  endDate: string;
  departmentId: number | null;
  departmentName: string | null;
  entries: AbsenteeEntry[];
  totalAbsentInstances: number;
}

export interface ReportScheduleRequest {
  reportName: string;
  reportType: string;
  frequency: string;
  departmentId: number | null;
  recipients: string | null;
  exportFormat: string;
}

export interface ReportScheduleResponse {
  id: number;
  reportName: string;
  reportType: string;
  frequency: string;
  departmentId: number | null;
  departmentName: string | null;
  recipients: string | null;
  exportFormat: string;
  active: boolean;
  lastRunAt: string | null;
  nextRunAt: string | null;
  createdAt: string;
  updatedAt: string;
}
