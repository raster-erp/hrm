import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { TaxSlabRequest, TaxSlabResponse } from '../models/tax-slab.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class TaxSlabService {
  private path = '/tax-slabs';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<TaxSlabResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<TaxSlabResponse>>(this.path, params);
  }

  getById(id: number): Observable<TaxSlabResponse> {
    return this.api.get<TaxSlabResponse>(`${this.path}/${id}`);
  }

  getByRegimeAndYear(regime: string, financialYear: string): Observable<TaxSlabResponse[]> {
    return this.api.get<TaxSlabResponse[]>(`${this.path}/regime/${regime}/year/${financialYear}`);
  }

  create(slab: TaxSlabRequest): Observable<TaxSlabResponse> {
    return this.api.post<TaxSlabResponse>(this.path, slab);
  }

  update(id: number, slab: TaxSlabRequest): Observable<TaxSlabResponse> {
    return this.api.put<TaxSlabResponse>(`${this.path}/${id}`, slab);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
