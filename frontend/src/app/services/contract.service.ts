import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { ContractRequest, ContractResponse, ContractAmendmentRequest, ContractAmendmentResponse } from '../models/contract.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class ContractService {
  private path = '/contracts';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<ContractResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<ContractResponse>>(this.path, params);
  }

  getById(id: number): Observable<ContractResponse> {
    return this.api.get<ContractResponse>(`${this.path}/${id}`);
  }

  getByEmployee(employeeId: number): Observable<ContractResponse[]> {
    return this.api.get<ContractResponse[]>(`${this.path}/employee/${employeeId}`);
  }

  getExpiring(startDate: string, endDate: string): Observable<ContractResponse[]> {
    const params = new HttpParams().set('startDate', startDate).set('endDate', endDate);
    return this.api.get<ContractResponse[]>(`${this.path}/expiring`, params);
  }

  create(contract: ContractRequest): Observable<ContractResponse> {
    return this.api.post<ContractResponse>(this.path, contract);
  }

  update(id: number, contract: ContractRequest): Observable<ContractResponse> {
    return this.api.put<ContractResponse>(`${this.path}/${id}`, contract);
  }

  renew(id: number): Observable<ContractResponse> {
    return this.api.post<ContractResponse>(`${this.path}/${id}/renew`, {});
  }

  addAmendment(id: number, amendment: ContractAmendmentRequest): Observable<ContractAmendmentResponse> {
    return this.api.post<ContractAmendmentResponse>(`${this.path}/${id}/amendments`, amendment);
  }

  getAmendments(id: number): Observable<ContractAmendmentResponse[]> {
    return this.api.get<ContractAmendmentResponse[]>(`${this.path}/${id}/amendments`);
  }
}
