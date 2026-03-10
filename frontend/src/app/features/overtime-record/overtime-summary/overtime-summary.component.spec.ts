import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { OvertimeSummaryComponent } from './overtime-summary.component';
import { OvertimeRecordService } from '../../../services/overtime-record.service';
import { NotificationService } from '../../../services/notification.service';
import { OvertimeSummaryResponse } from '../../../models/overtime-record.model';

describe('OvertimeSummaryComponent', () => {
  let component: OvertimeSummaryComponent;
  let fixture: ComponentFixture<OvertimeSummaryComponent>;
  let overtimeRecordServiceSpy: jasmine.SpyObj<OvertimeRecordService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  const mockSummary: OvertimeSummaryResponse[] = [
    {
      employeeId: 1,
      employeeCode: 'EMP001',
      employeeName: 'John Doe',
      totalOvertimeMinutes: 120,
      approvedOvertimeMinutes: 90,
      pendingOvertimeMinutes: 30,
      rejectedOvertimeMinutes: 0,
      weightedOvertimeMinutes: 135,
      recordCount: 3
    },
    {
      employeeId: 2,
      employeeCode: 'EMP002',
      employeeName: 'Jane Smith',
      totalOvertimeMinutes: 60,
      approvedOvertimeMinutes: 60,
      pendingOvertimeMinutes: 0,
      rejectedOvertimeMinutes: 0,
      weightedOvertimeMinutes: 90,
      recordCount: 1
    }
  ];

  beforeEach(async () => {
    overtimeRecordServiceSpy = jasmine.createSpyObj('OvertimeRecordService', ['getSummary']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    overtimeRecordServiceSpy.getSummary.and.returnValue(of(mockSummary));

    await TestBed.configureTestingModule({
      imports: [
        OvertimeSummaryComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: OvertimeRecordService, useValue: overtimeRecordServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(OvertimeSummaryComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have displayed columns defined', () => {
    expect(component.displayedColumns).toEqual([
      'employeeName', 'employeeCode', 'totalOvertimeMinutes', 'approvedOvertimeMinutes',
      'pendingOvertimeMinutes', 'rejectedOvertimeMinutes', 'weightedOvertimeMinutes', 'recordCount'
    ]);
  });

  it('should initialize with default date range', () => {
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    const expectedStart = firstDay.toISOString().substring(0, 10);
    const expectedEnd = today.toISOString().substring(0, 10);
    expect(component.startDate).toBe(expectedStart);
    expect(component.endDate).toBe(expectedEnd);
  });

  it('should not be loading initially', () => {
    expect(component.loading).toBe(false);
  });

  it('should load summary data', () => {
    component.loadSummary();
    expect(overtimeRecordServiceSpy.getSummary).toHaveBeenCalledWith(component.startDate, component.endDate);
    expect(component.dataSource.data.length).toBe(2);
    expect(component.loading).toBe(false);
  });

  it('should handle summary load error', () => {
    overtimeRecordServiceSpy.getSummary.and.returnValue(throwError(() => new Error('fail')));
    component.loadSummary();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load overtime summary');
    expect(component.loading).toBe(false);
  });

  it('should format minutes as hours and minutes', () => {
    expect(component.formatMinutes(90)).toBe('1h 30m');
    expect(component.formatMinutes(60)).toBe('1h 0m');
    expect(component.formatMinutes(45)).toBe('0h 45m');
    expect(component.formatMinutes(0)).toBe('0h 0m');
    expect(component.formatMinutes(125)).toBe('2h 5m');
  });

  it('should calculate total minutes for a given field', () => {
    component.dataSource.data = mockSummary;
    expect(component.getTotalMinutes('totalOvertimeMinutes')).toBe(180);
    expect(component.getTotalMinutes('approvedOvertimeMinutes')).toBe(150);
    expect(component.getTotalMinutes('pendingOvertimeMinutes')).toBe(30);
    expect(component.getTotalMinutes('rejectedOvertimeMinutes')).toBe(0);
    expect(component.getTotalMinutes('weightedOvertimeMinutes')).toBe(225);
  });

  it('should calculate total record count', () => {
    component.dataSource.data = mockSummary;
    expect(component.getTotalRecordCount()).toBe(4);
  });

  it('should return zero totals when no data', () => {
    component.dataSource.data = [];
    expect(component.getTotalMinutes('totalOvertimeMinutes')).toBe(0);
    expect(component.getTotalRecordCount()).toBe(0);
  });

  it('should have empty data source initially', () => {
    expect(component.dataSource.data.length).toBe(0);
  });
});
