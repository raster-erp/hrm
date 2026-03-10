import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { ShiftRosterRequest, ShiftRosterResponse, BulkShiftRosterRequest } from '../models/shift-roster.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class ShiftRosterService {
  private path = '/shift-rosters';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<ShiftRosterResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<ShiftRosterResponse>>(this.path, params);
  }

  getById(id: number): Observable<ShiftRosterResponse> {
    return this.api.get<ShiftRosterResponse>(`${this.path}/${id}`);
  }

  getByEmployeeId(employeeId: number): Observable<ShiftRosterResponse[]> {
    return this.api.get<ShiftRosterResponse[]>(`${this.path}/employee/${employeeId}`);
  }

  create(roster: ShiftRosterRequest): Observable<ShiftRosterResponse> {
    return this.api.post<ShiftRosterResponse>(this.path, roster);
  }

  bulkCreate(request: BulkShiftRosterRequest): Observable<ShiftRosterResponse[]> {
    return this.api.post<ShiftRosterResponse[]>(`${this.path}/bulk`, request);
  }

  update(id: number, roster: ShiftRosterRequest): Observable<ShiftRosterResponse> {
    return this.api.put<ShiftRosterResponse>(`${this.path}/${id}`, roster);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
