export interface PromotionRequest {
  employeeId: number;
  oldDesignationId: number;
  newDesignationId: number;
  oldGrade?: string;
  newGrade?: string;
  effectiveDate: string;
  reason?: string;
}

export interface PromotionResponse {
  id: number;
  employeeId: number;
  employeeName: string;
  oldDesignationId: number;
  oldDesignationName: string;
  newDesignationId: number;
  newDesignationName: string;
  oldGrade?: string;
  newGrade?: string;
  effectiveDate: string;
  reason?: string;
  status: string;
  approvedById?: number;
  approvedByName?: string;
  approvedAt?: string;
  createdAt: string;
  updatedAt: string;
}
