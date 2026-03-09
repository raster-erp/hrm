export interface CredentialRequest {
  employeeId: number;
  credentialType: string;
  credentialName: string;
  issuingAuthority: string;
  issueDate: string;
  expiryDate?: string;
  credentialNumber?: string;
  notes?: string;
}

export interface CredentialResponse {
  id: number;
  employeeId: number;
  employeeName: string;
  credentialType: string;
  credentialName: string;
  issuingAuthority: string;
  issueDate: string;
  expiryDate?: string;
  credentialNumber?: string;
  verificationStatus: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CredentialAttachmentResponse {
  id: number;
  credentialId: number;
  fileName: string;
  filePath: string;
  fileSize: number;
  contentType: string;
  createdAt: string;
}
