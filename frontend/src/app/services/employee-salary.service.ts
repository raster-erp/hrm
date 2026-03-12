import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  EmployeeSalaryDetailRequest,
  EmployeeSalaryDetailResponse,
  SalaryRevisionRequest
} from '../models/employee-salary.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class EmployeeSalaryService {
  private path = '/employee-salary-details';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<EmployeeSalaryDetailResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<EmployeeSalaryDetailResponse>>(this.path, params);
  }

  getById(id: number): Observable<EmployeeSalaryDetailResponse> {
    return this.api.get<EmployeeSalaryDetailResponse>(`${this.path}/${id}`);
  }

  getByEmployeeId(employeeId: number): Observable<EmployeeSalaryDetailResponse[]> {
    return this.api.get<EmployeeSalaryDetailResponse[]>(`${this.path}/employee/${employeeId}`);
  }

  create(detail: EmployeeSalaryDetailRequest): Observable<EmployeeSalaryDetailResponse> {
    return this.api.post<EmployeeSalaryDetailResponse>(this.path, detail);
  }

  revise(employeeId: number, revision: SalaryRevisionRequest): Observable<EmployeeSalaryDetailResponse> {
    return this.api.post<EmployeeSalaryDetailResponse>(`${this.path}/employee/${employeeId}/revise`, revision);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
