import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { IdCardRequest, IdCardResponse } from '../models/id-card.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class IdCardService {
  private path = '/id-cards';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<IdCardResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<IdCardResponse>>(this.path, params);
  }

  getById(id: number): Observable<IdCardResponse> {
    return this.api.get<IdCardResponse>(`${this.path}/${id}`);
  }

  getByEmployee(employeeId: number): Observable<IdCardResponse[]> {
    return this.api.get<IdCardResponse[]>(`${this.path}/employee/${employeeId}`);
  }

  create(idCard: IdCardRequest): Observable<IdCardResponse> {
    return this.api.post<IdCardResponse>(this.path, idCard);
  }

  update(id: number, idCard: IdCardRequest): Observable<IdCardResponse> {
    return this.api.put<IdCardResponse>(`${this.path}/${id}`, idCard);
  }

  updateStatus(id: number, status: string): Observable<IdCardResponse> {
    return this.api.patch<IdCardResponse>(`${this.path}/${id}/status`, { status });
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
