import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  WfhRequestCreateRequest,
  WfhRequestResponse,
  WfhApprovalRequest,
  WfhCheckInRequest,
  WfhCheckInResponse,
  WfhDashboardResponse,
} from '../models/wfh-request.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class WfhRequestService {
  private path = '/wfh-requests';
  private activityPath = '/wfh-activity-logs';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<WfhRequestResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<WfhRequestResponse>>(this.path, params);
  }

  getById(id: number): Observable<WfhRequestResponse> {
    return this.api.get<WfhRequestResponse>(`${this.path}/${id}`);
  }

  getByEmployee(employeeId: number, page = 0, size = 10): Observable<Page<WfhRequestResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<WfhRequestResponse>>(`${this.path}/employee/${employeeId}`, params);
  }

  getByStatus(status: string, page = 0, size = 10): Observable<Page<WfhRequestResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<WfhRequestResponse>>(`${this.path}/status/${status}`, params);
  }

  create(request: WfhRequestCreateRequest): Observable<WfhRequestResponse> {
    return this.api.post<WfhRequestResponse>(this.path, request);
  }

  update(id: number, request: WfhRequestCreateRequest): Observable<WfhRequestResponse> {
    return this.api.put<WfhRequestResponse>(`${this.path}/${id}`, request);
  }

  approve(id: number, approval: WfhApprovalRequest): Observable<WfhRequestResponse> {
    return this.api.patch<WfhRequestResponse>(`${this.path}/${id}/approve`, approval);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }

  getDashboard(startDate: string, endDate: string): Observable<WfhDashboardResponse[]> {
    const params = new HttpParams().set('startDate', startDate).set('endDate', endDate);
    return this.api.get<WfhDashboardResponse[]>(`${this.path}/dashboard`, params);
  }

  checkIn(request: WfhCheckInRequest): Observable<WfhCheckInResponse> {
    return this.api.post<WfhCheckInResponse>(`${this.activityPath}/check-in`, request);
  }

  checkOut(activityLogId: number): Observable<WfhCheckInResponse> {
    return this.api.patch<WfhCheckInResponse>(`${this.activityPath}/${activityLogId}/check-out`, {});
  }

  getActivityLogs(requestId: number): Observable<WfhCheckInResponse[]> {
    return this.api.get<WfhCheckInResponse[]>(`${this.activityPath}/request/${requestId}`);
  }

  getActiveSession(requestId: number): Observable<WfhCheckInResponse> {
    return this.api.get<WfhCheckInResponse>(`${this.activityPath}/request/${requestId}/active`);
  }
}
