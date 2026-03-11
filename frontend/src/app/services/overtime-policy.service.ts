import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { OvertimePolicyRequest, OvertimePolicyResponse } from '../models/overtime-policy.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class OvertimePolicyService {
  private path = '/overtime-policies';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<OvertimePolicyResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<OvertimePolicyResponse>>(this.path, params);
  }

  getById(id: number): Observable<OvertimePolicyResponse> {
    return this.api.get<OvertimePolicyResponse>(`${this.path}/${id}`);
  }

  getByType(type: string): Observable<OvertimePolicyResponse[]> {
    return this.api.get<OvertimePolicyResponse[]>(`${this.path}/type/${type}`);
  }

  getActive(): Observable<OvertimePolicyResponse[]> {
    return this.api.get<OvertimePolicyResponse[]>(`${this.path}/active`);
  }

  create(policy: OvertimePolicyRequest): Observable<OvertimePolicyResponse> {
    return this.api.post<OvertimePolicyResponse>(this.path, policy);
  }

  update(id: number, policy: OvertimePolicyRequest): Observable<OvertimePolicyResponse> {
    return this.api.put<OvertimePolicyResponse>(`${this.path}/${id}`, policy);
  }

  updateActive(id: number, active: boolean): Observable<OvertimePolicyResponse> {
    return this.api.patch<OvertimePolicyResponse>(`${this.path}/${id}/active`, { active });
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
