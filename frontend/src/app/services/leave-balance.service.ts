import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  LeaveBalanceResponse,
  LeaveTransactionResponse,
  BalanceAdjustmentRequest,
  YearEndProcessingRequest,
  YearEndSummaryResponse,
} from '../models/leave-balance.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class LeaveBalanceService {
  private path = '/leave-balances';

  constructor(private api: ApiService) {}

  getBalancesByEmployee(employeeId: number, year: number): Observable<LeaveBalanceResponse[]> {
    const params = new HttpParams().set('year', year);
    return this.api.get<LeaveBalanceResponse[]>(`${this.path}/employee/${employeeId}`, params);
  }

  getBalance(employeeId: number, leaveTypeId: number, year: number): Observable<LeaveBalanceResponse> {
    const params = new HttpParams().set('year', year);
    return this.api.get<LeaveBalanceResponse>(`${this.path}/employee/${employeeId}/leave-type/${leaveTypeId}`, params);
  }

  getTransactions(
    employeeId: number,
    page = 0,
    size = 10,
    leaveTypeId?: number,
    transactionType?: string
  ): Observable<Page<LeaveTransactionResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (leaveTypeId) {
      params = params.set('leaveTypeId', leaveTypeId);
    }
    if (transactionType) {
      params = params.set('transactionType', transactionType);
    }
    return this.api.get<Page<LeaveTransactionResponse>>(`${this.path}/employee/${employeeId}/transactions`, params);
  }

  adjustBalance(request: BalanceAdjustmentRequest): Observable<LeaveBalanceResponse> {
    return this.api.post<LeaveBalanceResponse>(`${this.path}/adjust`, request);
  }

  processYearEnd(request: YearEndProcessingRequest): Observable<YearEndSummaryResponse> {
    return this.api.post<YearEndSummaryResponse>(`${this.path}/year-end`, request);
  }
}
