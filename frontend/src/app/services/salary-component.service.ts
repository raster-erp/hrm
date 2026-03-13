import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { SalaryComponentRequest, SalaryComponentResponse } from '../models/salary-component.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class SalaryComponentService {
  private path = '/salary-components';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<SalaryComponentResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<SalaryComponentResponse>>(this.path, params);
  }

  getById(id: number): Observable<SalaryComponentResponse> {
    return this.api.get<SalaryComponentResponse>(`${this.path}/${id}`);
  }

  getByType(type: string): Observable<SalaryComponentResponse[]> {
    return this.api.get<SalaryComponentResponse[]>(`${this.path}/type/${type}`);
  }

  getActive(): Observable<SalaryComponentResponse[]> {
    return this.api.get<SalaryComponentResponse[]>(`${this.path}/active`);
  }

  create(component: SalaryComponentRequest): Observable<SalaryComponentResponse> {
    return this.api.post<SalaryComponentResponse>(this.path, component);
  }

  update(id: number, component: SalaryComponentRequest): Observable<SalaryComponentResponse> {
    return this.api.put<SalaryComponentResponse>(`${this.path}/${id}`, component);
  }

  updateActive(id: number, active: boolean): Observable<SalaryComponentResponse> {
    return this.api.patch<SalaryComponentResponse>(`${this.path}/${id}/active`, { active });
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
