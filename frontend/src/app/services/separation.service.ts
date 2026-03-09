import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  SeparationRequest, SeparationResponse,
  ExitChecklistRequest, ExitChecklistResponse,
  NoDuesRequest, NoDuesResponse
} from '../models/separation.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class SeparationService {
  private path = '/separations';
  private checklistPath = '/exit-checklists';
  private noDuesPath = '/no-dues';

  constructor(private api: ApiService) {}

  // Separations
  getAll(page = 0, size = 10): Observable<Page<SeparationResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<SeparationResponse>>(this.path, params);
  }

  getById(id: number): Observable<SeparationResponse> {
    return this.api.get<SeparationResponse>(`${this.path}/${id}`);
  }

  getByEmployee(employeeId: number): Observable<SeparationResponse[]> {
    return this.api.get<SeparationResponse[]>(`${this.path}/employee/${employeeId}`);
  }

  getPending(): Observable<SeparationResponse[]> {
    return this.api.get<SeparationResponse[]>(`${this.path}/pending`);
  }

  create(separation: SeparationRequest): Observable<SeparationResponse> {
    return this.api.post<SeparationResponse>(this.path, separation);
  }

  approve(id: number, approvedById: number): Observable<SeparationResponse> {
    const params = new HttpParams().set('approvedById', approvedById);
    return this.api.put<SeparationResponse>(`${this.path}/${id}/approve`, {}, params);
  }

  reject(id: number, approvedById: number): Observable<SeparationResponse> {
    const params = new HttpParams().set('approvedById', approvedById);
    return this.api.put<SeparationResponse>(`${this.path}/${id}/reject`, {}, params);
  }

  finalize(id: number): Observable<SeparationResponse> {
    return this.api.put<SeparationResponse>(`${this.path}/${id}/finalize`, {});
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }

  // Exit Checklists
  getChecklist(separationId: number): Observable<ExitChecklistResponse[]> {
    return this.api.get<ExitChecklistResponse[]>(`${this.checklistPath}/separation/${separationId}`);
  }

  addChecklistItem(item: ExitChecklistRequest): Observable<ExitChecklistResponse> {
    return this.api.post<ExitChecklistResponse>(this.checklistPath, item);
  }

  clearChecklistItem(id: number, clearedBy: string): Observable<ExitChecklistResponse> {
    const params = new HttpParams().set('clearedBy', clearedBy);
    return this.api.put<ExitChecklistResponse>(`${this.checklistPath}/${id}/clear`, {}, params);
  }

  deleteChecklistItem(id: number): Observable<void> {
    return this.api.delete<void>(`${this.checklistPath}/${id}`);
  }

  // No Dues
  getNoDues(separationId: number): Observable<NoDuesResponse[]> {
    return this.api.get<NoDuesResponse[]>(`${this.noDuesPath}/separation/${separationId}`);
  }

  addNoDues(noDues: NoDuesRequest): Observable<NoDuesResponse> {
    return this.api.post<NoDuesResponse>(this.noDuesPath, noDues);
  }

  clearNoDues(id: number, clearedBy: string): Observable<NoDuesResponse> {
    const params = new HttpParams().set('clearedBy', clearedBy);
    return this.api.put<NoDuesResponse>(`${this.noDuesPath}/${id}/clear`, {}, params);
  }

  deleteNoDues(id: number): Observable<void> {
    return this.api.delete<void>(`${this.noDuesPath}/${id}`);
  }
}
