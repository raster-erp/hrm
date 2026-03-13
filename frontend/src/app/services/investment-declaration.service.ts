import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { InvestmentDeclarationRequest, InvestmentDeclarationResponse, ProofSubmissionRequest, ProofVerificationRequest } from '../models/investment-declaration.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class InvestmentDeclarationService {
  private path = '/investment-declarations';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<InvestmentDeclarationResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<InvestmentDeclarationResponse>>(this.path, params);
  }

  getById(id: number): Observable<InvestmentDeclarationResponse> {
    return this.api.get<InvestmentDeclarationResponse>(`${this.path}/${id}`);
  }

  getByEmployeeAndYear(employeeId: number, financialYear: string): Observable<InvestmentDeclarationResponse> {
    return this.api.get<InvestmentDeclarationResponse>(`${this.path}/employee/${employeeId}/year/${financialYear}`);
  }

  getByFinancialYear(financialYear: string, page = 0, size = 10): Observable<Page<InvestmentDeclarationResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<InvestmentDeclarationResponse>>(`${this.path}/year/${financialYear}`, params);
  }

  create(declaration: InvestmentDeclarationRequest): Observable<InvestmentDeclarationResponse> {
    return this.api.post<InvestmentDeclarationResponse>(this.path, declaration);
  }

  update(id: number, declaration: InvestmentDeclarationRequest): Observable<InvestmentDeclarationResponse> {
    return this.api.put<InvestmentDeclarationResponse>(`${this.path}/${id}`, declaration);
  }

  submit(id: number): Observable<InvestmentDeclarationResponse> {
    return this.api.patch<InvestmentDeclarationResponse>(`${this.path}/${id}/submit`);
  }

  verify(id: number, verifiedBy: number): Observable<InvestmentDeclarationResponse> {
    return this.api.patch<InvestmentDeclarationResponse>(`${this.path}/${id}/verify`, { verifiedBy });
  }

  reject(id: number, remarks: string): Observable<InvestmentDeclarationResponse> {
    return this.api.patch<InvestmentDeclarationResponse>(`${this.path}/${id}/reject`, { remarks });
  }

  submitProof(request: ProofSubmissionRequest): Observable<void> {
    return this.api.post<void>(`${this.path}/proof/submit`, request);
  }

  verifyProof(request: ProofVerificationRequest): Observable<void> {
    return this.api.post<void>(`${this.path}/proof/verify`, request);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
