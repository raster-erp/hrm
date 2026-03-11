import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { LeavePolicyRequest, LeavePolicyResponse } from '../models/leave-policy.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class LeavePolicyService {
  private path = '/leave-policies';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<LeavePolicyResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<LeavePolicyResponse>>(this.path, params);
  }

  getById(id: number): Observable<LeavePolicyResponse> {
    return this.api.get<LeavePolicyResponse>(`${this.path}/${id}`);
  }

  getByLeaveTypeId(leaveTypeId: number): Observable<LeavePolicyResponse[]> {
    return this.api.get<LeavePolicyResponse[]>(`${this.path}/leave-type/${leaveTypeId}`);
  }

  getActive(): Observable<LeavePolicyResponse[]> {
    return this.api.get<LeavePolicyResponse[]>(`${this.path}/active`);
  }

  create(policy: LeavePolicyRequest): Observable<LeavePolicyResponse> {
    return this.api.post<LeavePolicyResponse>(this.path, policy);
  }

  update(id: number, policy: LeavePolicyRequest): Observable<LeavePolicyResponse> {
    return this.api.put<LeavePolicyResponse>(`${this.path}/${id}`, policy);
  }

  updateActive(id: number, active: boolean): Observable<LeavePolicyResponse> {
    return this.api.patch<LeavePolicyResponse>(`${this.path}/${id}/active`, { active });
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
