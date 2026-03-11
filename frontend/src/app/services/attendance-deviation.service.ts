import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  AttendanceDeviationRequest,
  AttendanceDeviationResponse,
  DeviationApprovalRequest,
  DeviationSummaryResponse,
} from '../models/attendance-deviation.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class AttendanceDeviationService {
  private path = '/attendance-deviations';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<AttendanceDeviationResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<AttendanceDeviationResponse>>(this.path, params);
  }

  getById(id: number): Observable<AttendanceDeviationResponse> {
    return this.api.get<AttendanceDeviationResponse>(`${this.path}/${id}`);
  }

  getByEmployee(employeeId: number, page = 0, size = 10): Observable<Page<AttendanceDeviationResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<AttendanceDeviationResponse>>(`${this.path}/employee/${employeeId}`, params);
  }

  getByType(type: string, page = 0, size = 10): Observable<Page<AttendanceDeviationResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<AttendanceDeviationResponse>>(`${this.path}/type/${type}`, params);
  }

  getByStatus(status: string, page = 0, size = 10): Observable<Page<AttendanceDeviationResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<AttendanceDeviationResponse>>(`${this.path}/status/${status}`, params);
  }

  getByDateRange(startDate: string, endDate: string, page = 0, size = 10): Observable<Page<AttendanceDeviationResponse>> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate)
      .set('page', page)
      .set('size', size);
    return this.api.get<Page<AttendanceDeviationResponse>>(`${this.path}/date-range`, params);
  }

  create(request: AttendanceDeviationRequest): Observable<AttendanceDeviationResponse> {
    return this.api.post<AttendanceDeviationResponse>(this.path, request);
  }

  update(id: number, request: AttendanceDeviationRequest): Observable<AttendanceDeviationResponse> {
    return this.api.put<AttendanceDeviationResponse>(`${this.path}/${id}`, request);
  }

  approve(id: number, approval: DeviationApprovalRequest): Observable<AttendanceDeviationResponse> {
    return this.api.patch<AttendanceDeviationResponse>(`${this.path}/${id}/approve`, approval);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }

  detect(employeeId: number, date: string): Observable<AttendanceDeviationResponse[]> {
    const params = new HttpParams()
      .set('employeeId', employeeId)
      .set('date', date);
    return this.api.post<AttendanceDeviationResponse[]>(`${this.path}/detect`, {}, params);
  }

  getSummary(startDate: string, endDate: string): Observable<DeviationSummaryResponse[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.api.get<DeviationSummaryResponse[]>(`${this.path}/summary`, params);
  }
}
