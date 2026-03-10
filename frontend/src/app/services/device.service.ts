import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { DeviceRequest, DeviceResponse } from '../models/device.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class DeviceService {
  private path = '/devices';

  constructor(private api: ApiService) {}

  getAll(page = 0, size = 10): Observable<Page<DeviceResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<DeviceResponse>>(this.path, params);
  }

  getById(id: number): Observable<DeviceResponse> {
    return this.api.get<DeviceResponse>(`${this.path}/${id}`);
  }

  getBySerialNumber(serialNumber: string): Observable<DeviceResponse> {
    return this.api.get<DeviceResponse>(`${this.path}/serial/${serialNumber}`);
  }

  getByStatus(status: string): Observable<DeviceResponse[]> {
    return this.api.get<DeviceResponse[]>(`${this.path}/status/${status}`);
  }

  getByType(type: string): Observable<DeviceResponse[]> {
    return this.api.get<DeviceResponse[]>(`${this.path}/type/${type}`);
  }

  create(device: DeviceRequest): Observable<DeviceResponse> {
    return this.api.post<DeviceResponse>(this.path, device);
  }

  update(id: number, device: DeviceRequest): Observable<DeviceResponse> {
    return this.api.put<DeviceResponse>(`${this.path}/${id}`, device);
  }

  updateStatus(id: number, status: string): Observable<DeviceResponse> {
    return this.api.patch<DeviceResponse>(`${this.path}/${id}/status`, { status });
  }

  recordSync(id: number): Observable<DeviceResponse> {
    return this.api.post<DeviceResponse>(`${this.path}/${id}/sync`, {});
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
