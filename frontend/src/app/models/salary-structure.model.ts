export interface SalaryStructureComponentRequest {
  salaryComponentId: number;
  computationType: string;
  percentageValue: number | null;
  fixedAmount: number | null;
  sortOrder: number;
}

export interface SalaryStructureComponentResponse {
  id: number;
  salaryComponentId: number;
  salaryComponentCode: string;
  salaryComponentName: string;
  salaryComponentType: string;
  computationType: string;
  percentageValue: number | null;
  fixedAmount: number | null;
  sortOrder: number;
  createdAt: string;
}

export interface SalaryStructureRequest {
  code: string;
  name: string;
  description: string;
  components: SalaryStructureComponentRequest[];
}

export interface SalaryStructureResponse {
  id: number;
  code: string;
  name: string;
  description: string;
  active: boolean;
  components: SalaryStructureComponentResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface SalaryStructureCloneRequest {
  newCode: string;
  newName: string;
}
