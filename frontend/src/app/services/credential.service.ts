import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { CredentialRequest, CredentialResponse, CredentialAttachmentResponse } from '../models/credential.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class CredentialService {
  private path = '/credentials';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<CredentialResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<CredentialResponse>>(this.path, params);
  }

  getById(id: number): Observable<CredentialResponse> {
    return this.api.get<CredentialResponse>(`${this.path}/${id}`);
  }

  getByEmployee(employeeId: number): Observable<CredentialResponse[]> {
    return this.api.get<CredentialResponse[]>(`${this.path}/employee/${employeeId}`);
  }

  getExpiring(days: number): Observable<CredentialResponse[]> {
    const params = new HttpParams().set('days', days);
    return this.api.get<CredentialResponse[]>(`${this.path}/expiring`, params);
  }

  getByStatus(status: string): Observable<CredentialResponse[]> {
    return this.api.get<CredentialResponse[]>(`${this.path}/status/${status}`);
  }

  create(credential: CredentialRequest): Observable<CredentialResponse> {
    return this.api.post<CredentialResponse>(this.path, credential);
  }

  update(id: number, credential: CredentialRequest): Observable<CredentialResponse> {
    return this.api.put<CredentialResponse>(`${this.path}/${id}`, credential);
  }

  updateStatus(id: number, status: string): Observable<CredentialResponse> {
    return this.api.patch<CredentialResponse>(`${this.path}/${id}/status`, { verificationStatus: status });
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }

  getAttachments(id: number): Observable<CredentialAttachmentResponse[]> {
    return this.api.get<CredentialAttachmentResponse[]>(`${this.path}/${id}/attachments`);
  }
}
