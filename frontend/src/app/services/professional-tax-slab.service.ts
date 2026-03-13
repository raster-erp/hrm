import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { ProfessionalTaxSlabRequest, ProfessionalTaxSlabResponse } from '../models/professional-tax-slab.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class ProfessionalTaxSlabService {
  private path = '/professional-tax-slabs';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<ProfessionalTaxSlabResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<ProfessionalTaxSlabResponse>>(this.path, params);
  }

  getById(id: number): Observable<ProfessionalTaxSlabResponse> {
    return this.api.get<ProfessionalTaxSlabResponse>(`${this.path}/${id}`);
  }

  getByState(state: string): Observable<ProfessionalTaxSlabResponse[]> {
    return this.api.get<ProfessionalTaxSlabResponse[]>(`${this.path}/state/${state}`);
  }

  create(slab: ProfessionalTaxSlabRequest): Observable<ProfessionalTaxSlabResponse> {
    return this.api.post<ProfessionalTaxSlabResponse>(this.path, slab);
  }

  update(id: number, slab: ProfessionalTaxSlabRequest): Observable<ProfessionalTaxSlabResponse> {
    return this.api.put<ProfessionalTaxSlabResponse>(`${this.path}/${id}`, slab);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }

  computeProfessionalTax(employeeId: number, month: number): Observable<{ professionalTax: number }> {
    return this.api.get<{ professionalTax: number }>(`${this.path}/compute/employee/${employeeId}/month/${month}`);
  }
}
