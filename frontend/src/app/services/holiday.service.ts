import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { HolidayRequest, HolidayResponse } from '../models/holiday.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class HolidayService {
  private path = '/holidays';

  constructor(private api: ApiService) {}

  create(request: HolidayRequest): Observable<HolidayResponse> {
    return this.api.post<HolidayResponse>(this.path, request);
  }

  update(id: number, request: HolidayRequest): Observable<HolidayResponse> {
    return this.api.put<HolidayResponse>(`${this.path}/${id}`, request);
  }

  getAll(page = 0, size = 10): Observable<Page<HolidayResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<HolidayResponse>>(this.path, params);
  }

  getById(id: number): Observable<HolidayResponse> {
    return this.api.get<HolidayResponse>(`${this.path}/${id}`);
  }

  getByType(type: string, page = 0, size = 10): Observable<Page<HolidayResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<HolidayResponse>>(`${this.path}/type/${type}`, params);
  }

  getByRegion(region: string, page = 0, size = 10): Observable<Page<HolidayResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<HolidayResponse>>(`${this.path}/region/${region}`, params);
  }

  getByDateRange(start: string, end: string): Observable<HolidayResponse[]> {
    const params = new HttpParams().set('start', start).set('end', end);
    return this.api.get<HolidayResponse[]>(`${this.path}/date-range`, params);
  }

  getActiveByDateRange(start: string, end: string, region?: string): Observable<HolidayResponse[]> {
    let params = new HttpParams().set('start', start).set('end', end);
    if (region) {
      params = params.set('region', region);
    }
    return this.api.get<HolidayResponse[]>(`${this.path}/active`, params);
  }

  deactivate(id: number): Observable<HolidayResponse> {
    return this.api.patch<HolidayResponse>(`${this.path}/${id}/deactivate`);
  }
}
