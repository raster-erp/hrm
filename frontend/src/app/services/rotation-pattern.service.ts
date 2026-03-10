import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { RotationPatternRequest, RotationPatternResponse } from '../models/rotation-pattern.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class RotationPatternService {
  private path = '/rotation-patterns';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<RotationPatternResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<RotationPatternResponse>>(this.path, params);
  }

  getById(id: number): Observable<RotationPatternResponse> {
    return this.api.get<RotationPatternResponse>(`${this.path}/${id}`);
  }

  create(pattern: RotationPatternRequest): Observable<RotationPatternResponse> {
    return this.api.post<RotationPatternResponse>(this.path, pattern);
  }

  update(id: number, pattern: RotationPatternRequest): Observable<RotationPatternResponse> {
    return this.api.put<RotationPatternResponse>(`${this.path}/${id}`, pattern);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
