export interface AttendancePunchRequest {
  employeeId: number;
  deviceId: number;
  punchTime: string;
  direction: string;
  rawData: string;
}

export interface AttendancePunchResponse {
  id: number;
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  deviceId: number;
  deviceSerialNumber: string;
  deviceName: string;
  punchTime: string;
  direction: string;
  rawData: string;
  normalized: boolean;
  source: string;
  createdAt: string;
  updatedAt: string;
}
