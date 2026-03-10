import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { AttendancePunchRequest, AttendancePunchResponse } from '../models/attendance-punch.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class AttendancePunchService {
  private path = '/attendance-punches';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<AttendancePunchResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<AttendancePunchResponse>>(this.path, params);
  }

  getById(id: number): Observable<AttendancePunchResponse> {
    return this.api.get<AttendancePunchResponse>(`${this.path}/${id}`);
  }

  getByEmployee(employeeId: number, page = 0, size = 10): Observable<Page<AttendancePunchResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<AttendancePunchResponse>>(`${this.path}/employee/${employeeId}`, params);
  }

  getByDateRange(from: string, to: string, page = 0, size = 10): Observable<Page<AttendancePunchResponse>> {
    const params = new HttpParams()
      .set('from', from)
      .set('to', to)
      .set('page', page)
      .set('size', size);
    return this.api.get<Page<AttendancePunchResponse>>(`${this.path}/date-range`, params);
  }

  getByEmployeeAndDateRange(employeeId: number, from: string, to: string, page = 0, size = 10): Observable<Page<AttendancePunchResponse>> {
    const params = new HttpParams()
      .set('from', from)
      .set('to', to)
      .set('page', page)
      .set('size', size);
    return this.api.get<Page<AttendancePunchResponse>>(`${this.path}/employee/${employeeId}/date-range`, params);
  }

  create(punch: AttendancePunchRequest): Observable<AttendancePunchResponse> {
    return this.api.post<AttendancePunchResponse>(this.path, punch);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
