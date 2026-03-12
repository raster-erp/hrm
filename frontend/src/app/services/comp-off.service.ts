import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  CompOffCreditRequest,
  CompOffCreditResponse,
  CompOffApprovalRequest,
  CompOffBalanceResponse,
} from '../models/comp-off.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class CompOffService {
  private path = '/comp-off-credits';

  constructor(private api: ApiService) {}

  create(request: CompOffCreditRequest): Observable<CompOffCreditResponse> {
    return this.api.post<CompOffCreditResponse>(this.path, request);
  }

  getAll(page = 0, size = 10): Observable<Page<CompOffCreditResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<CompOffCreditResponse>>(this.path, params);
  }

  getById(id: number): Observable<CompOffCreditResponse> {
    return this.api.get<CompOffCreditResponse>(`${this.path}/${id}`);
  }

  getByEmployee(employeeId: number, page = 0, size = 10): Observable<Page<CompOffCreditResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<CompOffCreditResponse>>(`${this.path}/employee/${employeeId}`, params);
  }

  getByStatus(status: string, page = 0, size = 10): Observable<Page<CompOffCreditResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<CompOffCreditResponse>>(`${this.path}/status/${status}`, params);
  }

  getBalance(employeeId: number): Observable<CompOffBalanceResponse> {
    return this.api.get<CompOffBalanceResponse>(`${this.path}/balance/${employeeId}`);
  }

  approve(id: number, request: CompOffApprovalRequest): Observable<CompOffCreditResponse> {
    return this.api.patch<CompOffCreditResponse>(`${this.path}/${id}/approve`, request);
  }

  expireCredits(): Observable<number> {
    return this.api.post<number>(`${this.path}/expire`, {});
  }
}
