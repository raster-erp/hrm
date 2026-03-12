import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  EncashmentEligibilityResponse,
  LeaveEncashmentRequest,
  LeaveEncashmentResponse,
  LeaveEncashmentApprovalRequest,
} from '../models/leave-encashment.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class LeaveEncashmentService {
  private path = '/leave-encashments';

  constructor(private api: ApiService) {}

  checkEligibility(employeeId: number, leaveTypeId: number, year: number): Observable<EncashmentEligibilityResponse> {
    const params = new HttpParams()
      .set('employeeId', employeeId)
      .set('leaveTypeId', leaveTypeId)
      .set('year', year);
    return this.api.get<EncashmentEligibilityResponse>(`${this.path}/eligibility`, params);
  }

  create(request: LeaveEncashmentRequest): Observable<LeaveEncashmentResponse> {
    return this.api.post<LeaveEncashmentResponse>(this.path, request);
  }

  getAll(page = 0, size = 10): Observable<Page<LeaveEncashmentResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<LeaveEncashmentResponse>>(this.path, params);
  }

  getById(id: number): Observable<LeaveEncashmentResponse> {
    return this.api.get<LeaveEncashmentResponse>(`${this.path}/${id}`);
  }

  getByEmployee(employeeId: number, page = 0, size = 10): Observable<Page<LeaveEncashmentResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<LeaveEncashmentResponse>>(`${this.path}/employee/${employeeId}`, params);
  }

  getByStatus(status: string, page = 0, size = 10): Observable<Page<LeaveEncashmentResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<LeaveEncashmentResponse>>(`${this.path}/status/${status}`, params);
  }

  approve(id: number, request: LeaveEncashmentApprovalRequest): Observable<LeaveEncashmentResponse> {
    return this.api.patch<LeaveEncashmentResponse>(`${this.path}/${id}/approve`, request);
  }

  markAsPaid(id: number, approvedBy: string): Observable<LeaveEncashmentResponse> {
    return this.api.patch<LeaveEncashmentResponse>(`${this.path}/${id}/pay`, { approvedBy });
  }
}
