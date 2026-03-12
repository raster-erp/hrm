import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { LeaveCalendarEntry, TeamAvailabilityResponse } from '../models/leave-calendar.model';

@Injectable({ providedIn: 'root' })
export class LeaveCalendarService {
  private path = '/leave-calendar';

  constructor(private api: ApiService) {}

  getCalendarEntries(
    start: string,
    end: string,
    employeeId?: number,
    departmentId?: number,
    region?: string
  ): Observable<LeaveCalendarEntry[]> {
    let params = new HttpParams().set('start', start).set('end', end);
    if (employeeId) {
      params = params.set('employeeId', employeeId);
    }
    if (departmentId) {
      params = params.set('departmentId', departmentId);
    }
    if (region) {
      params = params.set('region', region);
    }
    return this.api.get<LeaveCalendarEntry[]>(`${this.path}/entries`, params);
  }

  getTeamAvailability(
    departmentId: number,
    start: string,
    end: string
  ): Observable<TeamAvailabilityResponse[]> {
    const params = new HttpParams()
      .set('departmentId', departmentId)
      .set('start', start)
      .set('end', end);
    return this.api.get<TeamAvailabilityResponse[]>(`${this.path}/team-availability`, params);
  }
}
