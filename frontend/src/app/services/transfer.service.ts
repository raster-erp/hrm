import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { TransferRequest, TransferResponse } from '../models/transfer.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class TransferService {
  private path = '/transfers';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<TransferResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<TransferResponse>>(this.path, params);
  }

  getById(id: number): Observable<TransferResponse> {
    return this.api.get<TransferResponse>(`${this.path}/${id}`);
  }

  getByEmployee(employeeId: number): Observable<TransferResponse[]> {
    return this.api.get<TransferResponse[]>(`${this.path}/employee/${employeeId}`);
  }

  getPending(): Observable<TransferResponse[]> {
    return this.api.get<TransferResponse[]>(`${this.path}/pending`);
  }

  create(transfer: TransferRequest): Observable<TransferResponse> {
    return this.api.post<TransferResponse>(this.path, transfer);
  }

  approve(id: number, approvedById: number): Observable<TransferResponse> {
    const params = new HttpParams().set('approvedById', approvedById);
    return this.api.put<TransferResponse>(`${this.path}/${id}/approve`, {}, params);
  }

  reject(id: number, approvedById: number): Observable<TransferResponse> {
    const params = new HttpParams().set('approvedById', approvedById);
    return this.api.put<TransferResponse>(`${this.path}/${id}/reject`, {}, params);
  }

  execute(id: number): Observable<TransferResponse> {
    return this.api.put<TransferResponse>(`${this.path}/${id}/execute`, {});
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
