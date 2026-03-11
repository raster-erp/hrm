import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  DailyMusterReport,
  MonthlySummaryReport,
  AbsenteeListReport,
  ReportScheduleRequest,
  ReportScheduleResponse,
} from '../models/attendance-report.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class AttendanceReportService {
  private path = '/attendance-reports';

  constructor(private api: ApiService) {}

  getDailyMuster(date: string, departmentId?: number): Observable<DailyMusterReport> {
    let params = new HttpParams().set('date', date);
    if (departmentId) {
      params = params.set('departmentId', departmentId);
    }
    return this.api.get<DailyMusterReport>(`${this.path}/daily-muster`, params);
  }

  getMonthlySummary(year: number, month: number, departmentId?: number): Observable<MonthlySummaryReport> {
    let params = new HttpParams().set('year', year).set('month', month);
    if (departmentId) {
      params = params.set('departmentId', departmentId);
    }
    return this.api.get<MonthlySummaryReport>(`${this.path}/monthly-summary`, params);
  }

  getAbsenteeList(startDate: string, endDate: string, departmentId?: number): Observable<AbsenteeListReport> {
    let params = new HttpParams().set('startDate', startDate).set('endDate', endDate);
    if (departmentId) {
      params = params.set('departmentId', departmentId);
    }
    return this.api.get<AbsenteeListReport>(`${this.path}/absentee-list`, params);
  }

  exportReport(reportType: string, format: string, params: Record<string, string>): Observable<Blob> {
    let httpParams = new HttpParams().set('reportType', reportType).set('format', format);
    for (const key of Object.keys(params)) {
      httpParams = httpParams.set(key, params[key]);
    }
    return this.api.get<Blob>(`${this.path}/export`, httpParams);
  }

  getAllSchedules(page = 0, size = 10): Observable<Page<ReportScheduleResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.api.get<Page<ReportScheduleResponse>>(`${this.path}/schedules`, params);
  }

  getScheduleById(id: number): Observable<ReportScheduleResponse> {
    return this.api.get<ReportScheduleResponse>(`${this.path}/schedules/${id}`);
  }

  createSchedule(request: ReportScheduleRequest): Observable<ReportScheduleResponse> {
    return this.api.post<ReportScheduleResponse>(`${this.path}/schedules`, request);
  }

  updateSchedule(id: number, request: ReportScheduleRequest): Observable<ReportScheduleResponse> {
    return this.api.put<ReportScheduleResponse>(`${this.path}/schedules/${id}`, request);
  }

  toggleScheduleActive(id: number): Observable<ReportScheduleResponse> {
    return this.api.patch<ReportScheduleResponse>(`${this.path}/schedules/${id}/active`);
  }

  deleteSchedule(id: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/schedules/${id}`);
  }
}
