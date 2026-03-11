import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { ReportScheduleListComponent } from './report-schedule-list.component';
import { AttendanceReportService } from '../../../services/attendance-report.service';
import { NotificationService } from '../../../services/notification.service';
import { ReportScheduleResponse } from '../../../models/attendance-report.model';
import { Page } from '../../../models/page.model';

describe('ReportScheduleListComponent', () => {
  let component: ReportScheduleListComponent;
  let fixture: ComponentFixture<ReportScheduleListComponent>;
  let reportServiceSpy: jasmine.SpyObj<AttendanceReportService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  const mockSchedules: Page<ReportScheduleResponse> = {
    content: [
      {
        id: 1, reportName: 'Daily Report', reportType: 'DAILY_MUSTER',
        frequency: 'DAILY', departmentId: 1, departmentName: 'IT',
        recipients: 'admin@test.com', exportFormat: 'CSV',
        active: true, lastRunAt: '2025-01-15T08:00:00',
        nextRunAt: '2025-01-16T08:00:00',
        createdAt: '2025-01-01T00:00:00', updatedAt: '2025-01-01T00:00:00'
      },
      {
        id: 2, reportName: 'Monthly Summary', reportType: 'MONTHLY_SUMMARY',
        frequency: 'MONTHLY', departmentId: null, departmentName: null,
        recipients: null, exportFormat: 'CSV',
        active: false, lastRunAt: null, nextRunAt: null,
        createdAt: '2025-01-01T00:00:00', updatedAt: '2025-01-01T00:00:00'
      }
    ],
    totalElements: 2,
    totalPages: 1,
    size: 10,
    number: 0,
    first: true,
    last: true,
    empty: false
  };

  beforeEach(async () => {
    reportServiceSpy = jasmine.createSpyObj('AttendanceReportService', [
      'getAllSchedules', 'toggleScheduleActive', 'deleteSchedule'
    ]);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    reportServiceSpy.getAllSchedules.and.returnValue(of(mockSchedules));

    await TestBed.configureTestingModule({
      imports: [
        ReportScheduleListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule,
        RouterTestingModule
      ],
      providers: [
        { provide: AttendanceReportService, useValue: reportServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ReportScheduleListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load schedules on init', () => {
    expect(reportServiceSpy.getAllSchedules).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(2);
    expect(component.totalElements).toBe(2);
    expect(component.loading).toBe(false);
  });

  it('should handle page change', () => {
    component.onPageChange({ pageIndex: 1, pageSize: 5, length: 10 });
    expect(component.pageIndex).toBe(1);
    expect(component.pageSize).toBe(5);
    expect(reportServiceSpy.getAllSchedules).toHaveBeenCalledWith(1, 5);
  });

  it('should toggle schedule active status', () => {
    reportServiceSpy.toggleScheduleActive.and.returnValue(of({} as ReportScheduleResponse));
    component.toggleActive(mockSchedules.content[0]);
    expect(reportServiceSpy.toggleScheduleActive).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Schedule status updated');
  });

  it('should handle toggle active error', () => {
    reportServiceSpy.toggleScheduleActive.and.returnValue(throwError(() => new Error('fail')));
    component.toggleActive(mockSchedules.content[0]);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to update schedule status');
  });

  it('should delete schedule with confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    reportServiceSpy.deleteSchedule.and.returnValue(of(void 0));
    component.deleteSchedule(mockSchedules.content[0]);
    expect(reportServiceSpy.deleteSchedule).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Schedule deleted');
  });

  it('should not delete schedule without confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteSchedule(mockSchedules.content[0]);
    expect(reportServiceSpy.deleteSchedule).not.toHaveBeenCalled();
  });

  it('should handle delete error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    reportServiceSpy.deleteSchedule.and.returnValue(throwError(() => new Error('fail')));
    component.deleteSchedule(mockSchedules.content[0]);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to delete schedule');
  });

  it('should handle load schedules error', () => {
    reportServiceSpy.getAllSchedules.and.returnValue(throwError(() => new Error('fail')));
    component.loadSchedules();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load report schedules');
    expect(component.loading).toBe(false);
  });

  it('should return correct report labels', () => {
    expect(component.getReportLabel('DAILY_MUSTER')).toBe('Daily Muster');
    expect(component.getReportLabel('MONTHLY_SUMMARY')).toBe('Monthly Summary');
    expect(component.getReportLabel('ABSENTEE_LIST')).toBe('Absentee List');
    expect(component.getReportLabel('UNKNOWN')).toBe('UNKNOWN');
  });

  it('should have displayed columns defined', () => {
    expect(component.displayedColumns).toEqual([
      'reportName', 'reportType', 'frequency', 'departmentName',
      'exportFormat', 'active', 'lastRunAt', 'actions'
    ]);
  });
});
