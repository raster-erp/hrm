export interface InvestmentDeclarationItemRequest {
  section: string;
  description: string;
  declaredAmount: number;
}

export interface InvestmentDeclarationItemResponse {
  id: number;
  section: string;
  description: string;
  declaredAmount: number;
  verifiedAmount: number;
  proofStatus: string;
  proofDocumentName: string | null;
  proofRemarks: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface InvestmentDeclarationRequest {
  employeeId: number;
  financialYear: string;
  regime: string;
  remarks: string;
  items: InvestmentDeclarationItemRequest[];
}

export interface InvestmentDeclarationResponse {
  id: number;
  employeeId: number;
  employeeName: string;
  financialYear: string;
  regime: string;
  totalDeclaredAmount: number;
  totalVerifiedAmount: number;
  status: string;
  remarks: string | null;
  submittedAt: string | null;
  verifiedAt: string | null;
  verifiedBy: number | null;
  items: InvestmentDeclarationItemResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface ProofSubmissionRequest {
  itemId: number;
  proofDocumentName: string;
  declaredAmount: number | null;
}

export interface ProofVerificationRequest {
  itemId: number;
  verifiedAmount: number;
  status: string;
  remarks: string;
}
