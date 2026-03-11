import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { DeviationSummaryComponent } from './deviation-summary.component';
import { AttendanceDeviationService } from '../../../services/attendance-deviation.service';
import { NotificationService } from '../../../services/notification.service';
import { DeviationSummaryResponse } from '../../../models/attendance-deviation.model';

describe('DeviationSummaryComponent', () => {
  let component: DeviationSummaryComponent;
  let fixture: ComponentFixture<DeviationSummaryComponent>;
  let deviationServiceSpy: jasmine.SpyObj<AttendanceDeviationService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  const mockSummary: DeviationSummaryResponse[] = [
    {
      employeeId: 1,
      employeeCode: 'EMP001',
      employeeName: 'John Doe',
      lateComingCount: 5,
      earlyGoingCount: 2,
      totalDeviationMinutes: 120,
      lateComingMinutes: 90,
      earlyGoingMinutes: 30,
      warningCount: 3,
      leaveDeductionCount: 1,
      payCutCount: 0
    },
    {
      employeeId: 2,
      employeeCode: 'EMP002',
      employeeName: 'Jane Smith',
      lateComingCount: 3,
      earlyGoingCount: 1,
      totalDeviationMinutes: 60,
      lateComingMinutes: 45,
      earlyGoingMinutes: 15,
      warningCount: 2,
      leaveDeductionCount: 0,
      payCutCount: 0
    }
  ];

  beforeEach(async () => {
    deviationServiceSpy = jasmine.createSpyObj('AttendanceDeviationService', ['getSummary']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    deviationServiceSpy.getSummary.and.returnValue(of(mockSummary));

    await TestBed.configureTestingModule({
      imports: [
        DeviationSummaryComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: AttendanceDeviationService, useValue: deviationServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DeviationSummaryComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have displayed columns defined', () => {
    expect(component.displayedColumns).toEqual([
      'employeeName', 'employeeCode', 'lateComingCount', 'earlyGoingCount',
      'totalDeviationMinutes', 'lateComingMinutes', 'earlyGoingMinutes',
      'warningCount', 'leaveDeductionCount', 'payCutCount'
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
    expect(deviationServiceSpy.getSummary).toHaveBeenCalledWith(component.startDate, component.endDate);
    expect(component.dataSource.data.length).toBe(2);
    expect(component.loading).toBe(false);
  });

  it('should handle summary load error', () => {
    deviationServiceSpy.getSummary.and.returnValue(throwError(() => new Error('fail')));
    component.loadSummary();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load deviation summary');
    expect(component.loading).toBe(false);
  });

  it('should format minutes', () => {
    expect(component.formatMinutes(90)).toBe('1h 30m');
    expect(component.formatMinutes(60)).toBe('1h 0m');
    expect(component.formatMinutes(45)).toBe('0h 45m');
    expect(component.formatMinutes(0)).toBe('0h 0m');
    expect(component.formatMinutes(125)).toBe('2h 5m');
  });

  it('should calculate totals', () => {
    component.dataSource.data = mockSummary;
    expect(component.getTotalCount('lateComingCount')).toBe(8);
    expect(component.getTotalCount('earlyGoingCount')).toBe(3);
    expect(component.getTotalCount('totalDeviationMinutes')).toBe(180);
    expect(component.getTotalCount('lateComingMinutes')).toBe(135);
    expect(component.getTotalCount('earlyGoingMinutes')).toBe(45);
    expect(component.getTotalCount('warningCount')).toBe(5);
    expect(component.getTotalCount('leaveDeductionCount')).toBe(1);
    expect(component.getTotalCount('payCutCount')).toBe(0);
  });

  it('should have empty data source initially', () => {
    expect(component.dataSource.data.length).toBe(0);
  });
});
