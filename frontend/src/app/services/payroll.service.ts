import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  PayrollRunRequest,
  PayrollRunResponse,
  PayrollDetailResponse,
  PayrollAdjustmentRequest,
  PayrollAdjustmentResponse
} from '../models/payroll.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class PayrollService {
  private runPath = '/payroll-runs';
  private adjustmentPath = '/payroll-adjustments';

  constructor(private api: ApiService) {}

  getAllRuns(page = 0, size = 10): Observable<Page<PayrollRunResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<PayrollRunResponse>>(this.runPath, params);
  }

  getRunById(id: number): Observable<PayrollRunResponse> {
    return this.api.get<PayrollRunResponse>(`${this.runPath}/${id}`);
  }

  initializeRun(request: PayrollRunRequest): Observable<PayrollRunResponse> {
    return this.api.post<PayrollRunResponse>(this.runPath, request);
  }

  computePayroll(id: number): Observable<PayrollRunResponse> {
    return this.api.post<PayrollRunResponse>(`${this.runPath}/${id}/compute`, {});
  }

  getDetails(runId: number, page = 0, size = 10): Observable<Page<PayrollDetailResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<PayrollDetailResponse>>(`${this.runPath}/${runId}/details`, params);
  }

  getDetailByEmployee(runId: number, employeeId: number): Observable<PayrollDetailResponse> {
    return this.api.get<PayrollDetailResponse>(`${this.runPath}/${runId}/details/employee/${employeeId}`);
  }

  verifyRun(id: number): Observable<PayrollRunResponse> {
    return this.api.post<PayrollRunResponse>(`${this.runPath}/${id}/verify`, {});
  }

  finalizeRun(id: number): Observable<PayrollRunResponse> {
    return this.api.post<PayrollRunResponse>(`${this.runPath}/${id}/finalize`, {});
  }

  reverseRun(id: number): Observable<PayrollRunResponse> {
    return this.api.post<PayrollRunResponse>(`${this.runPath}/${id}/reverse`, {});
  }

  getAdjustments(runId: number): Observable<PayrollAdjustmentResponse[]> {
    return this.api.get<PayrollAdjustmentResponse[]>(`${this.adjustmentPath}/run/${runId}`);
  }

  createAdjustment(request: PayrollAdjustmentRequest): Observable<PayrollAdjustmentResponse> {
    return this.api.post<PayrollAdjustmentResponse>(this.adjustmentPath, request);
  }

  deleteAdjustment(id: number): Observable<void> {
    return this.api.delete<void>(`${this.adjustmentPath}/${id}`);
  }
}
