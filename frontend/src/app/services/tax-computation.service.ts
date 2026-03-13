import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { TaxComputationRequest, TaxComputationResponse, Form16DataResponse } from '../models/tax-computation.model';

@Injectable({ providedIn: 'root' })
export class TaxComputationService {
  private path = '/tax-computations';

  constructor(private api: ApiService) {}

  computeMonthlyTds(request: TaxComputationRequest): Observable<TaxComputationResponse> {
    return this.api.post<TaxComputationResponse>(`${this.path}/compute`, request);
  }

  getByEmployeeAndYear(employeeId: number, financialYear: string): Observable<TaxComputationResponse[]> {
    return this.api.get<TaxComputationResponse[]>(`${this.path}/employee/${employeeId}/year/${financialYear}`);
  }

  getByEmployeeYearMonth(employeeId: number, financialYear: string, month: number): Observable<TaxComputationResponse> {
    return this.api.get<TaxComputationResponse>(`${this.path}/employee/${employeeId}/year/${financialYear}/month/${month}`);
  }

  generateForm16Data(employeeId: number, financialYear: string): Observable<Form16DataResponse> {
    return this.api.get<Form16DataResponse>(`${this.path}/form16/employee/${employeeId}/year/${financialYear}`);
  }
}
