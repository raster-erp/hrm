import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { ReportViewerComponent } from './report-viewer.component';
import { AttendanceReportService } from '../../../services/attendance-report.service';
import { NotificationService } from '../../../services/notification.service';
import {
  DailyMusterReport,
  MonthlySummaryReport,
  AbsenteeListReport,
} from '../../../models/attendance-report.model';

describe('ReportViewerComponent', () => {
  let component: ReportViewerComponent;
  let fixture: ComponentFixture<ReportViewerComponent>;
  let reportServiceSpy: jasmine.SpyObj<AttendanceReportService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  const mockDailyMuster: DailyMusterReport = {
    date: '2025-01-15',
    departmentId: null,
    departmentName: 'All Departments',
    entries: [
      {
        employeeId: 1, employeeCode: 'EMP001', employeeName: 'John Doe',
        departmentName: 'IT', date: '2025-01-15',
        firstPunchIn: '2025-01-15T09:00:00', lastPunchOut: '2025-01-15T18:00:00',
        totalPunches: 2, status: 'PRESENT'
      },
      {
        employeeId: 2, employeeCode: 'EMP002', employeeName: 'Jane Smith',
        departmentName: 'HR', date: '2025-01-15',
        firstPunchIn: null, lastPunchOut: null,
        totalPunches: 0, status: 'ABSENT'
      }
    ],
    totalPresent: 1,
    totalAbsent: 1,
    totalIncomplete: 0
  };

  const mockMonthlySummary: MonthlySummaryReport = {
    year: 2025, month: 1,
    departmentId: null, departmentName: 'All Departments',
    entries: [
      {
        employeeId: 1, employeeCode: 'EMP001', employeeName: 'John Doe',
        departmentName: 'IT', totalPresent: 20, totalAbsent: 2, totalIncomplete: 0, totalWorkingDays: 22
      }
    ]
  };

  const mockAbsenteeList: AbsenteeListReport = {
    startDate: '2025-01-01', endDate: '2025-01-31',
    departmentId: null, departmentName: 'All Departments',
    entries: [
      {
        employeeId: 2, employeeCode: 'EMP002', employeeName: 'Jane Smith',
        departmentName: 'HR', absentDate: '2025-01-15'
      }
    ],
    totalAbsentInstances: 1
  };

  beforeEach(async () => {
    reportServiceSpy = jasmine.createSpyObj('AttendanceReportService', [
      'getDailyMuster', 'getMonthlySummary', 'getAbsenteeList', 'exportReport'
    ]);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    reportServiceSpy.getDailyMuster.and.returnValue(of(mockDailyMuster));
    reportServiceSpy.getMonthlySummary.and.returnValue(of(mockMonthlySummary));
    reportServiceSpy.getAbsenteeList.and.returnValue(of(mockAbsenteeList));

    await TestBed.configureTestingModule({
      imports: [
        ReportViewerComponent,
        HttpClientTestingModule,
        NoopAnimationsModule,
        RouterTestingModule
      ],
      providers: [
        { provide: AttendanceReportService, useValue: reportServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ReportViewerComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with DAILY_MUSTER report type', () => {
    expect(component.selectedReportType).toBe('DAILY_MUSTER');
  });

  it('should have report types defined', () => {
    expect(component.reportTypes).toEqual(['DAILY_MUSTER', 'MONTHLY_SUMMARY', 'ABSENTEE_LIST']);
  });

  it('should not be loading initially', () => {
    expect(component.loading).toBe(false);
  });

  it('should load daily muster report', () => {
    component.selectedReportType = 'DAILY_MUSTER';
    component.loadReport();
    expect(reportServiceSpy.getDailyMuster).toHaveBeenCalled();
    expect(component.dailyMusterReport).toEqual(mockDailyMuster);
    expect(component.dailyMusterDataSource.data.length).toBe(2);
    expect(component.loading).toBe(false);
  });

  it('should load monthly summary report', () => {
    component.selectedReportType = 'MONTHLY_SUMMARY';
    component.loadReport();
    expect(reportServiceSpy.getMonthlySummary).toHaveBeenCalled();
    expect(component.monthlySummaryReport).toEqual(mockMonthlySummary);
    expect(component.monthlySummaryDataSource.data.length).toBe(1);
    expect(component.loading).toBe(false);
  });

  it('should load absentee list report', () => {
    component.selectedReportType = 'ABSENTEE_LIST';
    component.loadReport();
    expect(reportServiceSpy.getAbsenteeList).toHaveBeenCalled();
    expect(component.absenteeListReport).toEqual(mockAbsenteeList);
    expect(component.absenteeDataSource.data.length).toBe(1);
    expect(component.loading).toBe(false);
  });

  it('should handle daily muster load error', () => {
    reportServiceSpy.getDailyMuster.and.returnValue(throwError(() => new Error('fail')));
    component.selectedReportType = 'DAILY_MUSTER';
    component.loadReport();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load daily muster report');
    expect(component.loading).toBe(false);
  });

  it('should handle monthly summary load error', () => {
    reportServiceSpy.getMonthlySummary.and.returnValue(throwError(() => new Error('fail')));
    component.selectedReportType = 'MONTHLY_SUMMARY';
    component.loadReport();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load monthly summary report');
    expect(component.loading).toBe(false);
  });

  it('should handle absentee list load error', () => {
    reportServiceSpy.getAbsenteeList.and.returnValue(throwError(() => new Error('fail')));
    component.selectedReportType = 'ABSENTEE_LIST';
    component.loadReport();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load absentee list report');
    expect(component.loading).toBe(false);
  });

  it('should return correct report labels', () => {
    expect(component.getReportLabel('DAILY_MUSTER')).toBe('Daily Muster');
    expect(component.getReportLabel('MONTHLY_SUMMARY')).toBe('Monthly Summary');
    expect(component.getReportLabel('ABSENTEE_LIST')).toBe('Absentee List');
    expect(component.getReportLabel('UNKNOWN')).toBe('UNKNOWN');
  });

  it('should return correct status classes', () => {
    expect(component.getStatusClass('PRESENT')).toBe('status-present');
    expect(component.getStatusClass('ABSENT')).toBe('status-absent');
    expect(component.getStatusClass('INCOMPLETE')).toBe('status-incomplete');
    expect(component.getStatusClass('UNKNOWN')).toBe('');
  });

  it('should clear previous report data when loading new report', () => {
    component.selectedReportType = 'DAILY_MUSTER';
    component.loadReport();
    expect(component.dailyMusterReport).toBeTruthy();
    expect(component.monthlySummaryReport).toBeNull();
    expect(component.absenteeListReport).toBeNull();
  });

  it('should initialize dates correctly', () => {
    const today = new Date();
    const expectedYear = today.getFullYear();
    const expectedMonth = today.getMonth() + 1;
    expect(component.year).toBe(expectedYear);
    expect(component.month).toBe(expectedMonth);
  });

  it('should download CSV for daily muster', () => {
    const blob = new Blob(['test'], { type: 'text/csv' });
    reportServiceSpy.exportReport.and.returnValue(of(blob));
    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:test');
    spyOn(window.URL, 'revokeObjectURL');

    component.selectedReportType = 'DAILY_MUSTER';
    component.downloadCsv();

    expect(reportServiceSpy.exportReport).toHaveBeenCalledWith('DAILY_MUSTER', 'CSV', jasmine.objectContaining({ date: component.date }));
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Report downloaded successfully');
  });

  it('should download CSV for monthly summary', () => {
    const blob = new Blob(['test'], { type: 'text/csv' });
    reportServiceSpy.exportReport.and.returnValue(of(blob));
    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:test');
    spyOn(window.URL, 'revokeObjectURL');

    component.selectedReportType = 'MONTHLY_SUMMARY';
    component.downloadCsv();

    expect(reportServiceSpy.exportReport).toHaveBeenCalledWith('MONTHLY_SUMMARY', 'CSV', jasmine.objectContaining({
      year: String(component.year),
      month: String(component.month)
    }));
  });

  it('should download CSV for absentee list', () => {
    const blob = new Blob(['test'], { type: 'text/csv' });
    reportServiceSpy.exportReport.and.returnValue(of(blob));
    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:test');
    spyOn(window.URL, 'revokeObjectURL');

    component.selectedReportType = 'ABSENTEE_LIST';
    component.downloadCsv();

    expect(reportServiceSpy.exportReport).toHaveBeenCalledWith('ABSENTEE_LIST', 'CSV', jasmine.objectContaining({
      startDate: component.startDate,
      endDate: component.endDate
    }));
  });

  it('should handle CSV download error', () => {
    reportServiceSpy.exportReport.and.returnValue(throwError(() => new Error('fail')));
    component.downloadCsv();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to download report');
  });

  it('should include departmentId in CSV download when set', () => {
    const blob = new Blob(['test'], { type: 'text/csv' });
    reportServiceSpy.exportReport.and.returnValue(of(blob));
    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:test');
    spyOn(window.URL, 'revokeObjectURL');

    component.departmentId = 5;
    component.selectedReportType = 'DAILY_MUSTER';
    component.downloadCsv();

    expect(reportServiceSpy.exportReport).toHaveBeenCalledWith('DAILY_MUSTER', 'CSV', jasmine.objectContaining({ departmentId: '5' }));
  });

  it('should load daily muster with departmentId when set', () => {
    component.selectedReportType = 'DAILY_MUSTER';
    component.departmentId = 3;
    component.loadReport();
    expect(reportServiceSpy.getDailyMuster).toHaveBeenCalledWith(component.date, 3);
  });

  it('should format date with zero-padded month and day', () => {
    // Verify date format through the initialized values
    expect(component.date).toMatch(/^\d{4}-\d{2}-\d{2}$/);
    expect(component.startDate).toMatch(/^\d{4}-\d{2}-\d{2}$/);
    expect(component.endDate).toMatch(/^\d{4}-\d{2}-\d{2}$/);
  });
});
