export interface DepartmentRequest {
  name: string;
  code: string;
  parentId?: number;
  description?: string;
  active: boolean;
}

export interface DepartmentResponse {
  id: number;
  name: string;
  code: string;
  parentId?: number;
  parentName?: string;
  description?: string;
  active: boolean;
  children?: DepartmentResponse[];
  createdAt: string;
  updatedAt: string;
}
