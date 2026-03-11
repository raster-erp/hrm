import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { LeavePolicyAssignmentRequest, LeavePolicyAssignmentResponse } from '../models/leave-policy-assignment.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class LeavePolicyAssignmentService {
  private path = '/leave-policy-assignments';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<LeavePolicyAssignmentResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<LeavePolicyAssignmentResponse>>(this.path, params);
  }

  getById(id: number): Observable<LeavePolicyAssignmentResponse> {
    return this.api.get<LeavePolicyAssignmentResponse>(`${this.path}/${id}`);
  }

  getByPolicyId(policyId: number): Observable<LeavePolicyAssignmentResponse[]> {
    return this.api.get<LeavePolicyAssignmentResponse[]>(`${this.path}/policy/${policyId}`);
  }

  getByType(type: string): Observable<LeavePolicyAssignmentResponse[]> {
    return this.api.get<LeavePolicyAssignmentResponse[]>(`${this.path}/type/${type}`);
  }

  getByDepartmentId(departmentId: number): Observable<LeavePolicyAssignmentResponse[]> {
    return this.api.get<LeavePolicyAssignmentResponse[]>(`${this.path}/department/${departmentId}`);
  }

  getByDesignationId(designationId: number): Observable<LeavePolicyAssignmentResponse[]> {
    return this.api.get<LeavePolicyAssignmentResponse[]>(`${this.path}/designation/${designationId}`);
  }

  getByEmployeeId(employeeId: number): Observable<LeavePolicyAssignmentResponse[]> {
    return this.api.get<LeavePolicyAssignmentResponse[]>(`${this.path}/employee/${employeeId}`);
  }

  create(assignment: LeavePolicyAssignmentRequest): Observable<LeavePolicyAssignmentResponse> {
    return this.api.post<LeavePolicyAssignmentResponse>(this.path, assignment);
  }

  update(id: number, assignment: LeavePolicyAssignmentRequest): Observable<LeavePolicyAssignmentResponse> {
    return this.api.put<LeavePolicyAssignmentResponse>(`${this.path}/${id}`, assignment);
  }

  updateActive(id: number, active: boolean): Observable<LeavePolicyAssignmentResponse> {
    return this.api.patch<LeavePolicyAssignmentResponse>(`${this.path}/${id}/active`, { active });
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
