import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AttendanceReportService } from './attendance-report.service';
import { ApiService } from './api.service';
import { DailyMusterReport, MonthlySummaryReport, AbsenteeListReport, ReportScheduleResponse } from '../models/attendance-report.model';
import { Page } from '../models/page.model';

describe('AttendanceReportService', () => {
  let service: AttendanceReportService;
  let apiSpy: jasmine.SpyObj<ApiService>;

  const mockDailyMuster: DailyMusterReport = {
    date: '2025-01-15', departmentId: null, departmentName: 'All',
    entries: [], totalPresent: 0, totalAbsent: 0, totalIncomplete: 0
  };

  const mockMonthlySummary: MonthlySummaryReport = {
    year: 2025, month: 1, departmentId: null, departmentName: 'All', entries: []
  };

  const mockAbsenteeList: AbsenteeListReport = {
    startDate: '2025-01-01', endDate: '2025-01-31',
    departmentId: null, departmentName: 'All', entries: [], totalAbsentInstances: 0
  };

  const mockSchedulePage: Page<ReportScheduleResponse> = {
    content: [], totalElements: 0, totalPages: 0, size: 10, number: 0,
    first: true, last: true, empty: true
  };

  beforeEach(() => {
    apiSpy = jasmine.createSpyObj('ApiService', ['get', 'post', 'put', 'patch', 'delete']);

    TestBed.configureTestingModule({
      providers: [
        AttendanceReportService,
        { provide: ApiService, useValue: apiSpy }
      ]
    });

    service = TestBed.inject(AttendanceReportService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get daily muster', () => {
    apiSpy.get.and.returnValue(of(mockDailyMuster));
    service.getDailyMuster('2025-01-15').subscribe(result => {
      expect(result).toEqual(mockDailyMuster);
    });
    expect(apiSpy.get).toHaveBeenCalled();
  });

  it('should get daily muster with department', () => {
    apiSpy.get.and.returnValue(of(mockDailyMuster));
    service.getDailyMuster('2025-01-15', 1).subscribe(result => {
      expect(result).toEqual(mockDailyMuster);
    });
    expect(apiSpy.get).toHaveBeenCalled();
  });

  it('should get monthly summary', () => {
    apiSpy.get.and.returnValue(of(mockMonthlySummary));
    service.getMonthlySummary(2025, 1).subscribe(result => {
      expect(result).toEqual(mockMonthlySummary);
    });
    expect(apiSpy.get).toHaveBeenCalled();
  });

  it('should get absentee list', () => {
    apiSpy.get.and.returnValue(of(mockAbsenteeList));
    service.getAbsenteeList('2025-01-01', '2025-01-31').subscribe(result => {
      expect(result).toEqual(mockAbsenteeList);
    });
    expect(apiSpy.get).toHaveBeenCalled();
  });

  it('should get all schedules', () => {
    apiSpy.get.and.returnValue(of(mockSchedulePage));
    service.getAllSchedules(0, 10).subscribe(result => {
      expect(result).toEqual(mockSchedulePage);
    });
    expect(apiSpy.get).toHaveBeenCalled();
  });

  it('should create schedule', () => {
    const request = { reportName: 'Test', reportType: 'DAILY_MUSTER', frequency: 'DAILY', departmentId: null, recipients: null, exportFormat: 'CSV' };
    apiSpy.post.and.returnValue(of({} as ReportScheduleResponse));
    service.createSchedule(request).subscribe();
    expect(apiSpy.post).toHaveBeenCalled();
  });

  it('should toggle schedule active', () => {
    apiSpy.patch.and.returnValue(of({} as ReportScheduleResponse));
    service.toggleScheduleActive(1).subscribe();
    expect(apiSpy.patch).toHaveBeenCalled();
  });

  it('should delete schedule', () => {
    apiSpy.delete.and.returnValue(of(void 0));
    service.deleteSchedule(1).subscribe();
    expect(apiSpy.delete).toHaveBeenCalled();
  });

  it('should update schedule', () => {
    const request = { reportName: 'Test', reportType: 'DAILY_MUSTER', frequency: 'DAILY', departmentId: null, recipients: null, exportFormat: 'CSV' };
    apiSpy.put.and.returnValue(of({} as ReportScheduleResponse));
    service.updateSchedule(1, request).subscribe();
    expect(apiSpy.put).toHaveBeenCalled();
  });

  it('should get schedule by id', () => {
    apiSpy.get.and.returnValue(of({} as ReportScheduleResponse));
    service.getScheduleById(1).subscribe();
    expect(apiSpy.get).toHaveBeenCalled();
  });

  it('should export report', () => {
    apiSpy.get.and.returnValue(of(new Blob()));
    service.exportReport('DAILY_MUSTER', 'CSV', { date: '2025-01-15' }).subscribe();
    expect(apiSpy.get).toHaveBeenCalled();
  });

  it('should get monthly summary with department', () => {
    apiSpy.get.and.returnValue(of(mockMonthlySummary));
    service.getMonthlySummary(2025, 1, 1).subscribe(result => {
      expect(result).toEqual(mockMonthlySummary);
    });
    expect(apiSpy.get).toHaveBeenCalled();
  });

  it('should get absentee list with department', () => {
    apiSpy.get.and.returnValue(of(mockAbsenteeList));
    service.getAbsenteeList('2025-01-01', '2025-01-31', 1).subscribe(result => {
      expect(result).toEqual(mockAbsenteeList);
    });
    expect(apiSpy.get).toHaveBeenCalled();
  });
});
