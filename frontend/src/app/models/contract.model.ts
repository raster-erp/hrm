export interface ContractRequest {
  employeeId: number;
  contractType: string;
  startDate: string;
  endDate: string;
  terms?: string;
  status: string;
}

export interface ContractResponse {
  id: number;
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  contractType: string;
  startDate: string;
  endDate: string;
  terms?: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface ContractAmendmentRequest {
  contractId: number;
  amendmentDate: string;
  description: string;
  oldTerms?: string;
  newTerms: string;
}

export interface ContractAmendmentResponse {
  id: number;
  contractId: number;
  amendmentDate: string;
  description: string;
  oldTerms?: string;
  newTerms: string;
  createdAt: string;
}
