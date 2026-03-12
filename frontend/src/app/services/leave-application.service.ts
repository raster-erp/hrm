import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  LeaveApplicationRequest,
  LeaveApplicationResponse,
  LeaveApprovalRequest,
} from '../models/leave-application.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class LeaveApplicationService {
  private path = '/leave-applications';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<LeaveApplicationResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<LeaveApplicationResponse>>(this.path, params);
  }

  getById(id: number): Observable<LeaveApplicationResponse> {
    return this.api.get<LeaveApplicationResponse>(`${this.path}/${id}`);
  }

  getByEmployee(employeeId: number, page = 0, size = 10): Observable<Page<LeaveApplicationResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<LeaveApplicationResponse>>(`${this.path}/employee/${employeeId}`, params);
  }

  getByStatus(status: string, page = 0, size = 10): Observable<Page<LeaveApplicationResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<LeaveApplicationResponse>>(`${this.path}/status/${status}`, params);
  }

  getByLeaveType(leaveTypeId: number, page = 0, size = 10): Observable<Page<LeaveApplicationResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<LeaveApplicationResponse>>(`${this.path}/leave-type/${leaveTypeId}`, params);
  }

  getByDateRange(fromDate: string, toDate: string, page = 0, size = 10): Observable<Page<LeaveApplicationResponse>> {
    const params = new HttpParams()
      .set('fromDate', fromDate)
      .set('toDate', toDate)
      .set('page', page)
      .set('size', size);
    return this.api.get<Page<LeaveApplicationResponse>>(`${this.path}/date-range`, params);
  }

  getByEmployeeAndDateRange(employeeId: number, fromDate: string, toDate: string, page = 0, size = 10): Observable<Page<LeaveApplicationResponse>> {
    const params = new HttpParams()
      .set('fromDate', fromDate)
      .set('toDate', toDate)
      .set('page', page)
      .set('size', size);
    return this.api.get<Page<LeaveApplicationResponse>>(`${this.path}/employee/${employeeId}/date-range`, params);
  }

  create(request: LeaveApplicationRequest): Observable<LeaveApplicationResponse> {
    return this.api.post<LeaveApplicationResponse>(this.path, request);
  }

  update(id: number, request: LeaveApplicationRequest): Observable<LeaveApplicationResponse> {
    return this.api.put<LeaveApplicationResponse>(`${this.path}/${id}`, request);
  }

  approve(id: number, approval: LeaveApprovalRequest): Observable<LeaveApplicationResponse> {
    return this.api.patch<LeaveApplicationResponse>(`${this.path}/${id}/approve`, approval);
  }

  cancel(id: number): Observable<LeaveApplicationResponse> {
    return this.api.patch<LeaveApplicationResponse>(`${this.path}/${id}/cancel`, {});
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
