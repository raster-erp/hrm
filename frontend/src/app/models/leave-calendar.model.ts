export interface LeaveCalendarEntry {
  id: number;
  type: string;
  employeeId: number | null;
  employeeName: string | null;
  leaveTypeName: string | null;
  leaveTypeCategory: string | null;
  fromDate: string;
  toDate: string;
  status: string;
  color: string;
}

export interface TeamAvailabilityResponse {
  date: string;
  totalMembers: number;
  availableMembers: number;
  onLeave: number;
  onPlannedLeave: number;
  coveragePercentage: number;
  absentEmployees: string[];
}
