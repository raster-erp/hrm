import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { UniformRequest, UniformResponse, UniformAllocationRequest, UniformAllocationResponse } from '../models/uniform.model';

@Injectable({ providedIn: 'root' })
export class UniformService {
  private path = '/uniforms';
  private allocationPath = '/uniform-allocations';

  constructor(private api: ApiService) {}

  getAll(): Observable<UniformResponse[]> {
    return this.api.get<UniformResponse[]>(this.path);
  }

  getById(id: number): Observable<UniformResponse> {
    return this.api.get<UniformResponse>(`${this.path}/${id}`);
  }

  create(uniform: UniformRequest): Observable<UniformResponse> {
    return this.api.post<UniformResponse>(this.path, uniform);
  }

  update(id: number, uniform: UniformRequest): Observable<UniformResponse> {
    return this.api.put<UniformResponse>(`${this.path}/${id}`, uniform);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }

  // Allocations
  getAllAllocations(): Observable<UniformAllocationResponse[]> {
    return this.api.get<UniformAllocationResponse[]>(this.allocationPath);
  }

  getAllocationsByEmployee(employeeId: number): Observable<UniformAllocationResponse[]> {
    return this.api.get<UniformAllocationResponse[]>(`${this.allocationPath}/employee/${employeeId}`);
  }

  allocate(allocation: UniformAllocationRequest): Observable<UniformAllocationResponse> {
    return this.api.post<UniformAllocationResponse>(this.allocationPath, allocation);
  }

  returnUniform(id: number): Observable<UniformAllocationResponse> {
    return this.api.put<UniformAllocationResponse>(`${this.allocationPath}/${id}/return`, {});
  }

  deleteAllocation(id: number): Observable<void> {
    return this.api.delete<void>(`${this.allocationPath}/${id}`);
  }
}
