import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { AnalyticsDashboardComponent } from './analytics-dashboard.component';
import { LeaveAnalyticsService } from '../../../services/leave-analytics.service';
import { NotificationService } from '../../../services/notification.service';
import {
  LeaveTrendReport,
  AbsenteeismRateReport,
  LeaveUtilizationReport,
} from '../../../models/leave-analytics.model';

describe('AnalyticsDashboardComponent', () => {
  let component: AnalyticsDashboardComponent;
  let fixture: ComponentFixture<AnalyticsDashboardComponent>;
  let analyticsServiceSpy: jasmine.SpyObj<LeaveAnalyticsService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  const mockTrendReport: LeaveTrendReport = {
    startYear: 2025, startMonth: 1, endYear: 2025, endMonth: 3,
    departmentId: null, departmentName: 'All Departments',
    entries: [
      { year: 2025, month: 1, leaveTypeName: 'Annual Leave', applicationCount: 5, totalDays: 10 },
      { year: 2025, month: 2, leaveTypeName: 'Annual Leave', applicationCount: 3, totalDays: 6 }
    ]
  };

  const mockAbsenteeismReport: AbsenteeismRateReport = {
    startDate: '2025-01-01', endDate: '2025-01-31',
    overallRate: 5.5,
    entries: [
      { departmentId: 1, departmentName: 'Engineering', employeeCount: 10, totalLeaveDays: 12, totalWorkingDays: 22, absenteeismRate: 5.45 }
    ]
  };

  const mockUtilizationReport: LeaveUtilizationReport = {
    year: 2025, departmentId: null, departmentName: 'All Departments',
    overallUtilization: 50,
    entries: [
      { employeeId: 1, employeeCode: 'EMP001', employeeName: 'John Doe', departmentName: 'Engineering', leaveTypeName: 'Annual Leave', entitled: 20, used: 10, available: 10, utilizationPercent: 50 }
    ]
  };

  beforeEach(async () => {
    analyticsServiceSpy = jasmine.createSpyObj('LeaveAnalyticsService', [
      'getLeaveTrend', 'getAbsenteeismRate', 'getLeaveUtilization', 'exportReport'
    ]);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    analyticsServiceSpy.getLeaveTrend.and.returnValue(of(mockTrendReport));
    analyticsServiceSpy.getAbsenteeismRate.and.returnValue(of(mockAbsenteeismReport));
    analyticsServiceSpy.getLeaveUtilization.and.returnValue(of(mockUtilizationReport));

    await TestBed.configureTestingModule({
      imports: [
        AnalyticsDashboardComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: LeaveAnalyticsService, useValue: analyticsServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AnalyticsDashboardComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with LEAVE_TREND report type', () => {
    expect(component.selectedReportType).toBe('LEAVE_TREND');
  });

  it('should have report types defined', () => {
    expect(component.reportTypes).toEqual(['LEAVE_TREND', 'ABSENTEEISM_RATE', 'LEAVE_UTILIZATION']);
  });

  it('should not be loading initially', () => {
    expect(component.loading).toBe(false);
  });

  it('should have export formats defined', () => {
    expect(component.exportFormats).toEqual(['CSV', 'EXCEL', 'PDF']);
  });

  it('should have dimension filter defaults', () => {
    expect(component.designationId).toBeNull();
    expect(component.gender).toBe('');
    expect(component.ageGroup).toBe('');
  });

  it('should load leave trend report', () => {
    component.selectedReportType = 'LEAVE_TREND';
    component.loadReport();
    expect(analyticsServiceSpy.getLeaveTrend).toHaveBeenCalled();
    expect(component.trendReport).toEqual(mockTrendReport);
    expect(component.trendDataSource.data.length).toBe(2);
    expect(component.loading).toBe(false);
  });

  it('should load absenteeism rate report', () => {
    component.selectedReportType = 'ABSENTEEISM_RATE';
    component.loadReport();
    expect(analyticsServiceSpy.getAbsenteeismRate).toHaveBeenCalled();
    expect(component.absenteeismReport).toEqual(mockAbsenteeismReport);
    expect(component.absenteeismDataSource.data.length).toBe(1);
    expect(component.loading).toBe(false);
  });

  it('should load leave utilization report', () => {
    component.selectedReportType = 'LEAVE_UTILIZATION';
    component.loadReport();
    expect(analyticsServiceSpy.getLeaveUtilization).toHaveBeenCalled();
    expect(component.utilizationReport).toEqual(mockUtilizationReport);
    expect(component.utilizationDataSource.data.length).toBe(1);
    expect(component.loading).toBe(false);
  });

  it('should handle leave trend load error', () => {
    analyticsServiceSpy.getLeaveTrend.and.returnValue(throwError(() => new Error('fail')));
    component.selectedReportType = 'LEAVE_TREND';
    component.loadReport();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load leave trend report');
    expect(component.loading).toBe(false);
  });

  it('should handle absenteeism rate load error', () => {
    analyticsServiceSpy.getAbsenteeismRate.and.returnValue(throwError(() => new Error('fail')));
    component.selectedReportType = 'ABSENTEEISM_RATE';
    component.loadReport();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load absenteeism rate report');
    expect(component.loading).toBe(false);
  });

  it('should handle leave utilization load error', () => {
    analyticsServiceSpy.getLeaveUtilization.and.returnValue(throwError(() => new Error('fail')));
    component.selectedReportType = 'LEAVE_UTILIZATION';
    component.loadReport();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load leave utilization report');
    expect(component.loading).toBe(false);
  });

  it('should return correct report labels', () => {
    expect(component.getReportLabel('LEAVE_TREND')).toBe('Leave Trend');
    expect(component.getReportLabel('ABSENTEEISM_RATE')).toBe('Absenteeism Rate');
    expect(component.getReportLabel('LEAVE_UTILIZATION')).toBe('Leave Utilization');
    expect(component.getReportLabel('UNKNOWN')).toBe('UNKNOWN');
  });

  it('should return correct month names', () => {
    expect(component.getMonthName(1)).toBe('January');
    expect(component.getMonthName(6)).toBe('June');
    expect(component.getMonthName(12)).toBe('December');
    expect(component.getMonthName(0)).toBe('');
  });

  it('should clear previous report data when loading new report', () => {
    component.selectedReportType = 'LEAVE_TREND';
    component.loadReport();
    expect(component.trendReport).toBeTruthy();
    expect(component.absenteeismReport).toBeNull();
    expect(component.utilizationReport).toBeNull();
  });

  it('should initialize dates correctly', () => {
    const today = new Date();
    const expectedYear = today.getFullYear();
    expect(component.year).toBe(expectedYear);
    expect(component.startYear).toBe(expectedYear);
    expect(component.endYear).toBe(expectedYear);
    expect(component.startMonth).toBe(1);
    expect(component.endMonth).toBe(today.getMonth() + 1);
  });

  it('should download CSV for leave trend', () => {
    const blob = new Blob(['test'], { type: 'text/csv' });
    analyticsServiceSpy.exportReport.and.returnValue(of(blob));
    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:test');
    spyOn(window.URL, 'revokeObjectURL');

    component.selectedReportType = 'LEAVE_TREND';
    component.downloadCsv();

    expect(analyticsServiceSpy.exportReport).toHaveBeenCalledWith('LEAVE_TREND', 'CSV', jasmine.objectContaining({
      startYear: String(component.startYear),
      startMonth: String(component.startMonth)
    }));
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Report downloaded successfully');
  });

  it('should download CSV for absenteeism rate', () => {
    const blob = new Blob(['test'], { type: 'text/csv' });
    analyticsServiceSpy.exportReport.and.returnValue(of(blob));
    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:test');
    spyOn(window.URL, 'revokeObjectURL');

    component.selectedReportType = 'ABSENTEEISM_RATE';
    component.downloadCsv();

    expect(analyticsServiceSpy.exportReport).toHaveBeenCalledWith('ABSENTEEISM_RATE', 'CSV', jasmine.objectContaining({
      startDate: component.startDate,
      endDate: component.endDate
    }));
  });

  it('should download CSV for leave utilization', () => {
    const blob = new Blob(['test'], { type: 'text/csv' });
    analyticsServiceSpy.exportReport.and.returnValue(of(blob));
    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:test');
    spyOn(window.URL, 'revokeObjectURL');

    component.selectedReportType = 'LEAVE_UTILIZATION';
    component.downloadCsv();

    expect(analyticsServiceSpy.exportReport).toHaveBeenCalledWith('LEAVE_UTILIZATION', 'CSV', jasmine.objectContaining({
      year: String(component.year)
    }));
  });

  it('should handle CSV download error', () => {
    analyticsServiceSpy.exportReport.and.returnValue(throwError(() => new Error('fail')));
    component.downloadCsv();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to download report');
  });

  it('should include departmentId in CSV download when set', () => {
    const blob = new Blob(['test'], { type: 'text/csv' });
    analyticsServiceSpy.exportReport.and.returnValue(of(blob));
    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:test');
    spyOn(window.URL, 'revokeObjectURL');

    component.departmentId = 5;
    component.selectedReportType = 'LEAVE_TREND';
    component.downloadCsv();

    expect(analyticsServiceSpy.exportReport).toHaveBeenCalledWith('LEAVE_TREND', 'CSV', jasmine.objectContaining({ departmentId: '5' }));
  });

  it('should load leave trend with departmentId when set', () => {
    component.selectedReportType = 'LEAVE_TREND';
    component.departmentId = 3;
    component.loadReport();
    expect(analyticsServiceSpy.getLeaveTrend).toHaveBeenCalledWith(
      component.startYear, component.startMonth, component.endYear, component.endMonth,
      3, undefined, undefined, undefined
    );
  });

  it('should format date with zero-padded month and day', () => {
    expect(component.startDate).toMatch(/^\d{4}-\d{2}-\d{2}$/);
    expect(component.endDate).toMatch(/^\d{4}-\d{2}-\d{2}$/);
  });

  it('should build trend chart data on load', () => {
    component.selectedReportType = 'LEAVE_TREND';
    component.loadReport();
    expect(component.trendChartData.labels!.length).toBeGreaterThan(0);
    expect(component.trendChartData.datasets.length).toBeGreaterThan(0);
  });

  it('should build absenteeism chart data on load', () => {
    component.selectedReportType = 'ABSENTEEISM_RATE';
    component.loadReport();
    expect(component.absenteeismChartData.labels!.length).toBe(1);
    expect(component.absenteeismChartData.datasets.length).toBe(1);
  });

  it('should build utilization chart data on load', () => {
    component.selectedReportType = 'LEAVE_UTILIZATION';
    component.loadReport();
    expect(component.utilizationChartData.labels!.length).toBe(1);
    expect(component.utilizationChartData.datasets.length).toBe(1);
  });

  it('should download Excel report', () => {
    const blob = new Blob(['test'], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    analyticsServiceSpy.exportReport.and.returnValue(of(blob));
    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:test');
    spyOn(window.URL, 'revokeObjectURL');

    component.selectedReportType = 'LEAVE_TREND';
    component.downloadReport('EXCEL');

    expect(analyticsServiceSpy.exportReport).toHaveBeenCalledWith('LEAVE_TREND', 'EXCEL', jasmine.any(Object));
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Report downloaded successfully');
  });

  it('should download PDF report', () => {
    const blob = new Blob(['test'], { type: 'application/pdf' });
    analyticsServiceSpy.exportReport.and.returnValue(of(blob));
    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:test');
    spyOn(window.URL, 'revokeObjectURL');

    component.selectedReportType = 'LEAVE_UTILIZATION';
    component.downloadReport('PDF');

    expect(analyticsServiceSpy.exportReport).toHaveBeenCalledWith('LEAVE_UTILIZATION', 'PDF', jasmine.any(Object));
  });

  it('should include dimension params in download when set', () => {
    const blob = new Blob(['test'], { type: 'text/csv' });
    analyticsServiceSpy.exportReport.and.returnValue(of(blob));
    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:test');
    spyOn(window.URL, 'revokeObjectURL');

    component.designationId = 2;
    component.gender = 'Female';
    component.ageGroup = '25_34';
    component.selectedReportType = 'LEAVE_TREND';
    component.downloadReport('CSV');

    expect(analyticsServiceSpy.exportReport).toHaveBeenCalledWith('LEAVE_TREND', 'CSV', jasmine.objectContaining({
      designationId: '2',
      gender: 'Female',
      ageGroup: '25_34'
    }));
  });

  it('should pass dimension params when loading report', () => {
    component.selectedReportType = 'LEAVE_TREND';
    component.designationId = 1;
    component.gender = 'Male';
    component.ageGroup = 'UNDER_25';
    component.loadReport();
    expect(analyticsServiceSpy.getLeaveTrend).toHaveBeenCalledWith(
      component.startYear, component.startMonth, component.endYear, component.endMonth,
      undefined, 1, 'Male', 'UNDER_25'
    );
  });

  it('should have age group options', () => {
    expect(component.ageGroupOptions.length).toBe(6);
    expect(component.ageGroupOptions[0].label).toBe('All');
  });

  it('should handle download error for non-CSV format', () => {
    analyticsServiceSpy.exportReport.and.returnValue(throwError(() => new Error('fail')));
    component.downloadReport('PDF');
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to download report');
  });
});
