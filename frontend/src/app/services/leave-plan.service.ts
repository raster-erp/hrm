import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { LeavePlanRequest, LeavePlanResponse } from '../models/leave-plan.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class LeavePlanService {
  private path = '/leave-plans';

  constructor(private api: ApiService) {}

  create(request: LeavePlanRequest): Observable<LeavePlanResponse> {
    return this.api.post<LeavePlanResponse>(this.path, request);
  }

  update(id: number, request: LeavePlanRequest): Observable<LeavePlanResponse> {
    return this.api.put<LeavePlanResponse>(`${this.path}/${id}`, request);
  }

  getAll(page = 0, size = 10): Observable<Page<LeavePlanResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<LeavePlanResponse>>(this.path, params);
  }

  getById(id: number): Observable<LeavePlanResponse> {
    return this.api.get<LeavePlanResponse>(`${this.path}/${id}`);
  }

  getByEmployee(employeeId: number, page = 0, size = 10): Observable<Page<LeavePlanResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<LeavePlanResponse>>(`${this.path}/employee/${employeeId}`, params);
  }

  getByStatus(status: string, page = 0, size = 10): Observable<Page<LeavePlanResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<LeavePlanResponse>>(`${this.path}/status/${status}`, params);
  }

  getByDateRange(start: string, end: string): Observable<LeavePlanResponse[]> {
    const params = new HttpParams().set('start', start).set('end', end);
    return this.api.get<LeavePlanResponse[]>(`${this.path}/date-range`, params);
  }

  getByEmployeeAndDateRange(employeeId: number, start: string, end: string): Observable<LeavePlanResponse[]> {
    const params = new HttpParams().set('start', start).set('end', end);
    return this.api.get<LeavePlanResponse[]>(`${this.path}/employee/${employeeId}/date-range`, params);
  }

  getByDepartmentAndDateRange(departmentId: number, start: string, end: string): Observable<LeavePlanResponse[]> {
    const params = new HttpParams().set('start', start).set('end', end);
    return this.api.get<LeavePlanResponse[]>(`${this.path}/department/${departmentId}/date-range`, params);
  }

  cancel(id: number): Observable<LeavePlanResponse> {
    return this.api.patch<LeavePlanResponse>(`${this.path}/${id}/cancel`);
  }

  convertToApplication(id: number): Observable<LeavePlanResponse> {
    return this.api.patch<LeavePlanResponse>(`${this.path}/${id}/convert`);
  }
}
