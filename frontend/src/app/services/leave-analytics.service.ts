import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  LeaveTrendReport,
  AbsenteeismRateReport,
  LeaveUtilizationReport,
} from '../models/leave-analytics.model';

@Injectable({ providedIn: 'root' })
export class LeaveAnalyticsService {
  private path = '/leave-analytics';

  constructor(private api: ApiService) {}

  getLeaveTrend(startYear: number, startMonth: number, endYear: number, endMonth: number,
                departmentId?: number, designationId?: number, gender?: string,
                ageGroup?: string): Observable<LeaveTrendReport> {
    let params = new HttpParams()
      .set('startYear', startYear)
      .set('startMonth', startMonth)
      .set('endYear', endYear)
      .set('endMonth', endMonth);
    if (departmentId) {
      params = params.set('departmentId', departmentId);
    }
    if (designationId) {
      params = params.set('designationId', designationId);
    }
    if (gender) {
      params = params.set('gender', gender);
    }
    if (ageGroup) {
      params = params.set('ageGroup', ageGroup);
    }
    return this.api.get<LeaveTrendReport>(`${this.path}/trend`, params);
  }

  getAbsenteeismRate(startDate: string, endDate: string,
                     departmentId?: number, designationId?: number, gender?: string,
                     ageGroup?: string): Observable<AbsenteeismRateReport> {
    let params = new HttpParams().set('startDate', startDate).set('endDate', endDate);
    if (departmentId) {
      params = params.set('departmentId', departmentId);
    }
    if (designationId) {
      params = params.set('designationId', designationId);
    }
    if (gender) {
      params = params.set('gender', gender);
    }
    if (ageGroup) {
      params = params.set('ageGroup', ageGroup);
    }
    return this.api.get<AbsenteeismRateReport>(`${this.path}/absenteeism-rate`, params);
  }

  getLeaveUtilization(year: number, departmentId?: number, designationId?: number,
                      gender?: string, ageGroup?: string): Observable<LeaveUtilizationReport> {
    let params = new HttpParams().set('year', year);
    if (departmentId) {
      params = params.set('departmentId', departmentId);
    }
    if (designationId) {
      params = params.set('designationId', designationId);
    }
    if (gender) {
      params = params.set('gender', gender);
    }
    if (ageGroup) {
      params = params.set('ageGroup', ageGroup);
    }
    return this.api.get<LeaveUtilizationReport>(`${this.path}/utilization`, params);
  }

  exportReport(reportType: string, format: string, params: Record<string, string>): Observable<Blob> {
    let httpParams = new HttpParams().set('reportType', reportType).set('format', format);
    for (const key of Object.keys(params)) {
      httpParams = httpParams.set(key, params[key]);
    }
    return this.api.get<Blob>(`${this.path}/export`, httpParams);
  }
}
