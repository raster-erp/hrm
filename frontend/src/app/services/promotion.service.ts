import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { PromotionRequest, PromotionResponse } from '../models/promotion.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class PromotionService {
  private path = '/promotions';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<PromotionResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<PromotionResponse>>(this.path, params);
  }

  getById(id: number): Observable<PromotionResponse> {
    return this.api.get<PromotionResponse>(`${this.path}/${id}`);
  }

  getByEmployee(employeeId: number): Observable<PromotionResponse[]> {
    return this.api.get<PromotionResponse[]>(`${this.path}/employee/${employeeId}`);
  }

  getPending(): Observable<PromotionResponse[]> {
    return this.api.get<PromotionResponse[]>(`${this.path}/pending`);
  }

  create(promotion: PromotionRequest): Observable<PromotionResponse> {
    return this.api.post<PromotionResponse>(this.path, promotion);
  }

  approve(id: number, approvedById: number): Observable<PromotionResponse> {
    const params = new HttpParams().set('approvedById', approvedById);
    return this.api.put<PromotionResponse>(`${this.path}/${id}/approve`, {}, params);
  }

  reject(id: number, approvedById: number): Observable<PromotionResponse> {
    const params = new HttpParams().set('approvedById', approvedById);
    return this.api.put<PromotionResponse>(`${this.path}/${id}/reject`, {}, params);
  }

  execute(id: number): Observable<PromotionResponse> {
    return this.api.put<PromotionResponse>(`${this.path}/${id}/execute`, {});
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
