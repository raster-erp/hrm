import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  OvertimeRecordRequest,
  OvertimeRecordResponse,
  OvertimeApprovalRequest,
  OvertimeSummaryResponse,
} from '../models/overtime-record.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class OvertimeRecordService {
  private path = '/overtime-records';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<OvertimeRecordResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<OvertimeRecordResponse>>(this.path, params);
  }

  getById(id: number): Observable<OvertimeRecordResponse> {
    return this.api.get<OvertimeRecordResponse>(`${this.path}/${id}`);
  }

  getByEmployee(employeeId: number, page = 0, size = 10): Observable<Page<OvertimeRecordResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<OvertimeRecordResponse>>(`${this.path}/employee/${employeeId}`, params);
  }

  getByStatus(status: string, page = 0, size = 10): Observable<Page<OvertimeRecordResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<OvertimeRecordResponse>>(`${this.path}/status/${status}`, params);
  }

  getByDateRange(startDate: string, endDate: string, page = 0, size = 10): Observable<Page<OvertimeRecordResponse>> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate)
      .set('page', page)
      .set('size', size);
    return this.api.get<Page<OvertimeRecordResponse>>(`${this.path}/date-range`, params);
  }

  create(record: OvertimeRecordRequest): Observable<OvertimeRecordResponse> {
    return this.api.post<OvertimeRecordResponse>(this.path, record);
  }

  update(id: number, record: OvertimeRecordRequest): Observable<OvertimeRecordResponse> {
    return this.api.put<OvertimeRecordResponse>(`${this.path}/${id}`, record);
  }

  approve(id: number, approval: OvertimeApprovalRequest): Observable<OvertimeRecordResponse> {
    return this.api.patch<OvertimeRecordResponse>(`${this.path}/${id}/approve`, approval);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }

  detectOvertime(employeeId: number, date: string, policyId: number): Observable<OvertimeRecordResponse> {
    const params = new HttpParams()
      .set('employeeId', employeeId)
      .set('date', date)
      .set('policyId', policyId);
    return this.api.post<OvertimeRecordResponse>(`${this.path}/detect`, {}, params);
  }

  getSummary(startDate: string, endDate: string): Observable<OvertimeSummaryResponse[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.api.get<OvertimeSummaryResponse[]>(`${this.path}/summary`, params);
  }
}
