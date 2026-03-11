import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { LeaveTypeRequest, LeaveTypeResponse } from '../models/leave-type.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class LeaveTypeService {
  private path = '/leave-types';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<LeaveTypeResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<LeaveTypeResponse>>(this.path, params);
  }

  getById(id: number): Observable<LeaveTypeResponse> {
    return this.api.get<LeaveTypeResponse>(`${this.path}/${id}`);
  }

  getByCategory(category: string): Observable<LeaveTypeResponse[]> {
    return this.api.get<LeaveTypeResponse[]>(`${this.path}/category/${category}`);
  }

  getActive(): Observable<LeaveTypeResponse[]> {
    return this.api.get<LeaveTypeResponse[]>(`${this.path}/active`);
  }

  create(leaveType: LeaveTypeRequest): Observable<LeaveTypeResponse> {
    return this.api.post<LeaveTypeResponse>(this.path, leaveType);
  }

  update(id: number, leaveType: LeaveTypeRequest): Observable<LeaveTypeResponse> {
    return this.api.put<LeaveTypeResponse>(`${this.path}/${id}`, leaveType);
  }

  updateActive(id: number, active: boolean): Observable<LeaveTypeResponse> {
    return this.api.patch<LeaveTypeResponse>(`${this.path}/${id}/active`, { active });
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
