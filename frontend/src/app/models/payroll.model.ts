export interface PayrollRunRequest {
  periodYear: number;
  periodMonth: number;
  notes: string;
}

export interface PayrollRunResponse {
  id: number;
  periodYear: number;
  periodMonth: number;
  runDate: string;
  status: string;
  totalGross: number;
  totalDeductions: number;
  totalNet: number;
  employeeCount: number;
  notes: string;
  createdAt: string;
  updatedAt: string;
}

export interface PayrollDetailResponse {
  id: number;
  payrollRunId: number;
  employeeId: number;
  employeeName: string;
  employeeCode: string;
  salaryStructureId: number;
  salaryStructureName: string;
  basicSalary: number;
  grossSalary: number;
  totalDeductions: number;
  netSalary: number;
  componentBreakup: string;
  daysPayable: number;
  lopDays: number;
  createdAt: string;
  updatedAt: string;
}

export interface PayrollAdjustmentRequest {
  payrollRunId: number;
  employeeId: number;
  adjustmentType: string;
  componentName: string;
  amount: number;
  reason: string;
}

export interface PayrollAdjustmentResponse {
  id: number;
  payrollRunId: number;
  employeeId: number;
  employeeName: string;
  employeeCode: string;
  adjustmentType: string;
  componentName: string;
  amount: number;
  reason: string;
  createdAt: string;
}
