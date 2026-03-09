import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { EmployeeRequest, EmployeeResponse, EmployeeSearchCriteria, EmployeeDocumentResponse } from '../models/employee.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private path = '/employees';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<EmployeeResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<EmployeeResponse>>(this.path, params);
  }

  getById(id: number): Observable<EmployeeResponse> {
    return this.api.get<EmployeeResponse>(`${this.path}/${id}`);
  }

  search(criteria: EmployeeSearchCriteria, page = 0, size = 10): Observable<Page<EmployeeResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (criteria.name) params = params.set('name', criteria.name);
    if (criteria.departmentId) params = params.set('departmentId', criteria.departmentId);
    if (criteria.status) params = params.set('status', criteria.status);
    if (criteria.joiningDateFrom) params = params.set('joiningDateFrom', criteria.joiningDateFrom);
    if (criteria.joiningDateTo) params = params.set('joiningDateTo', criteria.joiningDateTo);
    return this.api.get<Page<EmployeeResponse>>(`${this.path}/search`, params);
  }

  create(employee: EmployeeRequest): Observable<EmployeeResponse> {
    return this.api.post<EmployeeResponse>(this.path, employee);
  }

  update(id: number, employee: EmployeeRequest): Observable<EmployeeResponse> {
    return this.api.put<EmployeeResponse>(`${this.path}/${id}`, employee);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }

  getDocuments(id: number): Observable<EmployeeDocumentResponse[]> {
    return this.api.get<EmployeeDocumentResponse[]>(`${this.path}/${id}/documents`);
  }
}
