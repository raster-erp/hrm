import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { ShiftRequest, ShiftResponse } from '../models/shift.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class ShiftService {
  private path = '/shifts';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<ShiftResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<ShiftResponse>>(this.path, params);
  }

  getById(id: number): Observable<ShiftResponse> {
    return this.api.get<ShiftResponse>(`${this.path}/${id}`);
  }

  getByType(type: string): Observable<ShiftResponse[]> {
    return this.api.get<ShiftResponse[]>(`${this.path}/type/${type}`);
  }

  getActive(): Observable<ShiftResponse[]> {
    return this.api.get<ShiftResponse[]>(`${this.path}/active`);
  }

  create(shift: ShiftRequest): Observable<ShiftResponse> {
    return this.api.post<ShiftResponse>(this.path, shift);
  }

  update(id: number, shift: ShiftRequest): Observable<ShiftResponse> {
    return this.api.put<ShiftResponse>(`${this.path}/${id}`, shift);
  }

  updateActive(id: number, active: boolean): Observable<ShiftResponse> {
    return this.api.patch<ShiftResponse>(`${this.path}/${id}/active`, { active });
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
