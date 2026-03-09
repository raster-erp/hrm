export interface IdCardRequest {
  employeeId: number;
  issueDate: string;
  expiryDate: string;
}

export interface IdCardResponse {
  id: number;
  employeeId: number;
  employeeName: string;
  employeeCode: string;
  cardNumber: string;
  issueDate: string;
  expiryDate: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}
