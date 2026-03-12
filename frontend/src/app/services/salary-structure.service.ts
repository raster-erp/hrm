import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  SalaryStructureRequest,
  SalaryStructureResponse,
  SalaryStructureCloneRequest
} from '../models/salary-structure.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class SalaryStructureService {
  private path = '/salary-structures';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<SalaryStructureResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<SalaryStructureResponse>>(this.path, params);
  }

  getById(id: number): Observable<SalaryStructureResponse> {
    return this.api.get<SalaryStructureResponse>(`${this.path}/${id}`);
  }

  getActive(): Observable<SalaryStructureResponse[]> {
    return this.api.get<SalaryStructureResponse[]>(`${this.path}/active`);
  }

  create(structure: SalaryStructureRequest): Observable<SalaryStructureResponse> {
    return this.api.post<SalaryStructureResponse>(this.path, structure);
  }

  update(id: number, structure: SalaryStructureRequest): Observable<SalaryStructureResponse> {
    return this.api.put<SalaryStructureResponse>(`${this.path}/${id}`, structure);
  }

  clone(id: number, request: SalaryStructureCloneRequest): Observable<SalaryStructureResponse> {
    return this.api.post<SalaryStructureResponse>(`${this.path}/${id}/clone`, request);
  }

  updateActive(id: number, active: boolean): Observable<SalaryStructureResponse> {
    return this.api.patch<SalaryStructureResponse>(`${this.path}/${id}/active`, { active });
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
