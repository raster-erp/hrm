import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { WfhDashboardComponent } from './wfh-dashboard.component';
import { WfhRequestService } from '../../../services/wfh-request.service';
import { NotificationService } from '../../../services/notification.service';
import { WfhDashboardResponse } from '../../../models/wfh-request.model';

describe('WfhDashboardComponent', () => {
  let component: WfhDashboardComponent;
  let fixture: ComponentFixture<WfhDashboardComponent>;
  let wfhRequestServiceSpy: jasmine.SpyObj<WfhRequestService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  const mockDashboard: WfhDashboardResponse[] = [
    {
      employeeId: 1, employeeCode: 'EMP001', employeeName: 'John Doe',
      totalRequests: 5, approvedRequests: 3, pendingRequests: 1,
      rejectedRequests: 1, checkedInToday: true
    },
    {
      employeeId: 2, employeeCode: 'EMP002', employeeName: 'Jane Smith',
      totalRequests: 3, approvedRequests: 2, pendingRequests: 1,
      rejectedRequests: 0, checkedInToday: false
    }
  ];

  beforeEach(async () => {
    wfhRequestServiceSpy = jasmine.createSpyObj('WfhRequestService', ['getDashboard']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    wfhRequestServiceSpy.getDashboard.and.returnValue(of(mockDashboard));

    await TestBed.configureTestingModule({
      imports: [
        WfhDashboardComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: WfhRequestService, useValue: wfhRequestServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(WfhDashboardComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have displayed columns defined', () => {
    expect(component.displayedColumns).toEqual([
      'employeeName', 'employeeCode', 'totalRequests', 'approvedRequests',
      'pendingRequests', 'rejectedRequests', 'checkedInToday'
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

  it('should load dashboard data', () => {
    component.loadDashboard();
    expect(wfhRequestServiceSpy.getDashboard).toHaveBeenCalledWith(component.startDate, component.endDate);
    expect(component.dataSource.data.length).toBe(2);
    expect(component.loading).toBe(false);
  });

  it('should handle dashboard load error', () => {
    wfhRequestServiceSpy.getDashboard.and.returnValue(throwError(() => new Error('fail')));
    component.loadDashboard();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load WFH dashboard');
    expect(component.loading).toBe(false);
  });

  it('should calculate total requests for a given field', () => {
    component.dataSource.data = mockDashboard;
    expect(component.getTotalRequests('totalRequests')).toBe(8);
    expect(component.getTotalRequests('approvedRequests')).toBe(5);
    expect(component.getTotalRequests('pendingRequests')).toBe(2);
    expect(component.getTotalRequests('rejectedRequests')).toBe(1);
  });

  it('should calculate total checked in today', () => {
    component.dataSource.data = mockDashboard;
    expect(component.getTotalCheckedIn()).toBe(1);
  });

  it('should return zero totals when no data', () => {
    component.dataSource.data = [];
    expect(component.getTotalRequests('totalRequests')).toBe(0);
    expect(component.getTotalCheckedIn()).toBe(0);
  });

  it('should have empty data source initially', () => {
    expect(component.dataSource.data.length).toBe(0);
  });
});
