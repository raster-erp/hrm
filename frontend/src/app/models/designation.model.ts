export interface DesignationRequest {
  title: string;
  code: string;
  level: number;
  grade: string;
  departmentId: number;
  description?: string;
}

export interface DesignationResponse {
  id: number;
  title: string;
  code: string;
  level: number;
  grade: string;
  departmentId: number;
  departmentName: string;
  description?: string;
  createdAt: string;
  updatedAt: string;
}
