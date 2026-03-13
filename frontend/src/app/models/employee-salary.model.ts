export interface EmployeeSalaryDetailRequest {
  employeeId: number;
  salaryStructureId: number;
  ctc: number;
  basicSalary: number;
  effectiveDate: string;
  notes: string;
}

export interface EmployeeSalaryDetailResponse {
  id: number;
  employeeId: number;
  employeeName: string;
  employeeCode: string;
  salaryStructureId: number;
  salaryStructureName: string;
  ctc: number;
  basicSalary: number;
  effectiveDate: string;
  notes: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface SalaryRevisionRequest {
  salaryStructureId: number;
  ctc: number;
  basicSalary: number;
  effectiveDate: string;
  notes: string;
}
