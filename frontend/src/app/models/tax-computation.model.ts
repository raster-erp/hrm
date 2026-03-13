import { InvestmentDeclarationItemResponse } from './investment-declaration.model';

export interface TaxComputationRequest {
  employeeId: number;
  financialYear: string;
  month: number;
}

export interface TaxComputationResponse {
  id: number;
  employeeId: number;
  employeeName: string;
  financialYear: string;
  month: number;
  grossAnnualIncome: number;
  totalExemptions: number;
  taxableIncome: number;
  totalAnnualTax: number;
  monthlyTds: number;
  cess: number;
  surcharge: number;
  tdsDeductedTillDate: number;
  remainingTds: number;
  regime: string;
  createdAt: string;
  updatedAt: string;
}

export interface Form16DataResponse {
  employeeId: number;
  employeeName: string;
  financialYear: string;
  regime: string;
  grossAnnualIncome: number;
  totalExemptions: number;
  taxableIncome: number;
  totalTaxPayable: number;
  totalTdsDeducted: number;
  cess: number;
  surcharge: number;
  verifiedInvestments: InvestmentDeclarationItemResponse[];
  monthlyBreakup: TaxComputationResponse[];
}
