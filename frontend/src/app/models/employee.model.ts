export interface EmployeeRequest {
  employeeCode: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  dateOfBirth: string;
  gender: string;
  address: string;
  city: string;
  state: string;
  country: string;
  zipCode: string;
  emergencyContactName: string;
  emergencyContactPhone: string;
  bankName: string;
  bankAccountNumber: string;
  bankIfscCode: string;
  departmentId: number;
  designationId: number;
  joiningDate: string;
  employmentStatus: string;
}

export interface EmployeeResponse {
  id: number;
  employeeCode: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  dateOfBirth: string;
  gender: string;
  address: string;
  city: string;
  state: string;
  country: string;
  zipCode: string;
  emergencyContactName: string;
  emergencyContactPhone: string;
  bankName: string;
  bankAccountNumber: string;
  bankIfscCode: string;
  departmentId: number;
  departmentName: string;
  designationId: number;
  designationName: string;
  joiningDate: string;
  employmentStatus: string;
  photoUrl: string;
  createdAt: string;
  updatedAt: string;
}

export interface EmployeeSearchCriteria {
  name?: string;
  departmentId?: number;
  status?: string;
  joiningDateFrom?: string;
  joiningDateTo?: string;
}

export interface EmployeeDocumentResponse {
  id: number;
  employeeId: number;
  documentType: string;
  documentPath: string;
  fileSize: number;
  contentType: string;
  createdAt: string;
}
