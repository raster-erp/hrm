export interface SalaryComponentRequest {
  code: string;
  name: string;
  type: string;
  computationType: string;
  percentageValue: number | null;
  taxable: boolean;
  mandatory: boolean;
  description: string;
}

export interface SalaryComponentResponse {
  id: number;
  code: string;
  name: string;
  type: string;
  computationType: string;
  percentageValue: number | null;
  taxable: boolean;
  mandatory: boolean;
  description: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}
