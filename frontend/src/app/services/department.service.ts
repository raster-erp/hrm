import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { DepartmentRequest, DepartmentResponse } from '../models/department.model';

@Injectable({ providedIn: 'root' })
export class DepartmentService {
  private path = '/departments';

  constructor(private api: ApiService) {}

  getAll(): Observable<DepartmentResponse[]> {
    return this.api.get<DepartmentResponse[]>(this.path);
  }

  getById(id: number): Observable<DepartmentResponse> {
    return this.api.get<DepartmentResponse>(`${this.path}/${id}`);
  }

  getRootDepartments(): Observable<DepartmentResponse[]> {
    return this.api.get<DepartmentResponse[]>(`${this.path}/root`);
  }

  getChildren(parentId: number): Observable<DepartmentResponse[]> {
    return this.api.get<DepartmentResponse[]>(`${this.path}/parent/${parentId}`);
  }

  create(department: DepartmentRequest): Observable<DepartmentResponse> {
    return this.api.post<DepartmentResponse>(this.path, department);
  }

  update(id: number, department: DepartmentRequest): Observable<DepartmentResponse> {
    return this.api.put<DepartmentResponse>(`${this.path}/${id}`, department);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
