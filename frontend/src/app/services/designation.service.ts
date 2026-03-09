import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { DesignationRequest, DesignationResponse } from '../models/designation.model';

@Injectable({ providedIn: 'root' })
export class DesignationService {
  private path = '/designations';

  constructor(private api: ApiService) {}

  getAll(): Observable<DesignationResponse[]> {
    return this.api.get<DesignationResponse[]>(this.path);
  }

  getById(id: number): Observable<DesignationResponse> {
    return this.api.get<DesignationResponse>(`${this.path}/${id}`);
  }

  getByDepartment(departmentId: number): Observable<DesignationResponse[]> {
    return this.api.get<DesignationResponse[]>(`${this.path}/department/${departmentId}`);
  }

  create(designation: DesignationRequest): Observable<DesignationResponse> {
    return this.api.post<DesignationResponse>(this.path, designation);
  }

  update(id: number, designation: DesignationRequest): Observable<DesignationResponse> {
    return this.api.put<DesignationResponse>(`${this.path}/${id}`, designation);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
