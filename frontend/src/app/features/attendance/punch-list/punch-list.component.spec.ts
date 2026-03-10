import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { PunchListComponent } from './punch-list.component';
import { AttendancePunchService } from '../../../services/attendance-punch.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { AttendancePunchResponse } from '../../../models/attendance-punch.model';

describe('PunchListComponent', () => {
  let component: PunchListComponent;
  let fixture: ComponentFixture<PunchListComponent>;
  let punchServiceSpy: jasmine.SpyObj<AttendancePunchService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  const mockPage: Page<AttendancePunchResponse> = {
    content: [
      {
        id: 1, employeeId: 1, employeeCode: 'EMP001', employeeName: 'John Doe',
        deviceId: 1, deviceSerialNumber: 'BIO-001', deviceName: 'Main Entrance',
        punchTime: '2026-03-10T09:00:00', direction: 'IN', rawData: '',
        normalized: true, source: 'DEVICE',
        createdAt: '2026-03-10T09:00:00', updatedAt: '2026-03-10T09:00:00'
      }
    ],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    punchServiceSpy = jasmine.createSpyObj('AttendancePunchService',
      ['getAll', 'getByEmployee', 'getByDateRange', 'getByEmployeeAndDateRange']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    punchServiceSpy.getAll.and.returnValue(of(mockPage));
    punchServiceSpy.getByEmployee.and.returnValue(of(mockPage));
    punchServiceSpy.getByDateRange.and.returnValue(of(mockPage));
    punchServiceSpy.getByEmployeeAndDateRange.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        PunchListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: AttendancePunchService, useValue: punchServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({}),
            snapshot: { paramMap: { get: () => null } }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PunchListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have displayed columns defined', () => {
    expect(component.displayedColumns.length).toBeGreaterThan(0);
  });

  it('should have default page size of 10', () => {
    expect(component.pageSize).toBe(10);
  });

  it('should load punches on init', () => {
    component.ngOnInit();
    expect(punchServiceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle punch load error', () => {
    punchServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadPunches();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load punch records');
  });

  it('should filter by employee ID', () => {
    component.employeeId = 1;
    component.loadPunches();
    expect(punchServiceSpy.getByEmployee).toHaveBeenCalledWith(1, 0, 10);
  });

  it('should handle employee filter error', () => {
    punchServiceSpy.getByEmployee.and.returnValue(throwError(() => new Error('fail')));
    component.employeeId = 1;
    component.loadPunches();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load punch records');
  });

  it('should filter by date range', () => {
    component.dateFrom = new Date(2026, 2, 1);
    component.dateTo = new Date(2026, 2, 10);
    component.loadPunches();
    expect(punchServiceSpy.getByDateRange).toHaveBeenCalledWith('2026-03-01', '2026-03-10', 0, 10);
  });

  it('should handle date range filter error', () => {
    punchServiceSpy.getByDateRange.and.returnValue(throwError(() => new Error('fail')));
    component.dateFrom = new Date(2026, 2, 1);
    component.dateTo = new Date(2026, 2, 10);
    component.loadPunches();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load punch records');
  });

  it('should filter by employee and date range combined', () => {
    component.employeeId = 1;
    component.dateFrom = new Date(2026, 2, 1);
    component.dateTo = new Date(2026, 2, 10);
    component.loadPunches();
    expect(punchServiceSpy.getByEmployeeAndDateRange).toHaveBeenCalledWith(1, '2026-03-01', '2026-03-10', 0, 10);
  });

  it('should handle combined filter error', () => {
    punchServiceSpy.getByEmployeeAndDateRange.and.returnValue(throwError(() => new Error('fail')));
    component.employeeId = 1;
    component.dateFrom = new Date(2026, 2, 1);
    component.dateTo = new Date(2026, 2, 10);
    component.loadPunches();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load punch records');
  });

  it('should reset page index on search', () => {
    component.pageIndex = 5;
    component.onSearch();
    expect(component.pageIndex).toBe(0);
  });

  it('should update pagination on page change', () => {
    component.onPageChange({ pageIndex: 2, pageSize: 25, length: 100 });
    expect(component.pageIndex).toBe(2);
    expect(component.pageSize).toBe(25);
  });

  it('should clear all filters', () => {
    component.employeeId = 1;
    component.dateFrom = new Date();
    component.dateTo = new Date();
    component.clearFilters();
    expect(component.employeeId).toBeNull();
    expect(component.dateFrom).toBeNull();
    expect(component.dateTo).toBeNull();
  });

  it('should return correct direction icon', () => {
    expect(component.getDirectionIcon('IN')).toBe('login');
    expect(component.getDirectionIcon('OUT')).toBe('logout');
  });

  it('should return correct direction class', () => {
    expect(component.getDirectionClass('IN')).toBe('direction-in');
    expect(component.getDirectionClass('OUT')).toBe('direction-out');
  });
});
