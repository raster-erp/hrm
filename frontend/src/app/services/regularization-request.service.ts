import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  RegularizationRequestRequest,
  RegularizationRequestResponse,
  RegularizationApprovalRequest,
} from '../models/regularization-request.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class RegularizationRequestService {
  private path = '/regularization-requests';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<RegularizationRequestResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<RegularizationRequestResponse>>(this.path, params);
  }

  getById(id: number): Observable<RegularizationRequestResponse> {
    return this.api.get<RegularizationRequestResponse>(`${this.path}/${id}`);
  }

  getByEmployee(employeeId: number, page = 0, size = 10): Observable<Page<RegularizationRequestResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<RegularizationRequestResponse>>(`${this.path}/employee/${employeeId}`, params);
  }

  getByStatus(status: string, page = 0, size = 10): Observable<Page<RegularizationRequestResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<RegularizationRequestResponse>>(`${this.path}/status/${status}`, params);
  }

  getByType(type: string, page = 0, size = 10): Observable<Page<RegularizationRequestResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<RegularizationRequestResponse>>(`${this.path}/type/${type}`, params);
  }

  getByDateRange(startDate: string, endDate: string, page = 0, size = 10): Observable<Page<RegularizationRequestResponse>> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate)
      .set('page', page)
      .set('size', size);
    return this.api.get<Page<RegularizationRequestResponse>>(`${this.path}/date-range`, params);
  }

  getByEmployeeAndDateRange(employeeId: number, startDate: string, endDate: string, page = 0, size = 10): Observable<Page<RegularizationRequestResponse>> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate)
      .set('page', page)
      .set('size', size);
    return this.api.get<Page<RegularizationRequestResponse>>(`${this.path}/employee/${employeeId}/date-range`, params);
  }

  create(request: RegularizationRequestRequest): Observable<RegularizationRequestResponse> {
    return this.api.post<RegularizationRequestResponse>(this.path, request);
  }

  update(id: number, request: RegularizationRequestRequest): Observable<RegularizationRequestResponse> {
    return this.api.put<RegularizationRequestResponse>(`${this.path}/${id}`, request);
  }

  approve(id: number, approval: RegularizationApprovalRequest): Observable<RegularizationRequestResponse> {
    return this.api.patch<RegularizationRequestResponse>(`${this.path}/${id}/approve`, approval);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
