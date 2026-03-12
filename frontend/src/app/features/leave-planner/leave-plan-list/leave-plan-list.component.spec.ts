import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { LeavePlanListComponent } from './leave-plan-list.component';
import { LeavePlanService } from '../../../services/leave-plan.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { LeavePlanResponse } from '../../../models/leave-plan.model';

describe('LeavePlanListComponent', () => {
  let component: LeavePlanListComponent;
  let fixture: ComponentFixture<LeavePlanListComponent>;
  let leavePlanServiceSpy: jasmine.SpyObj<LeavePlanService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  const mockRecord: LeavePlanResponse = {
    id: 1, employeeId: 100, employeeCode: 'EMP001', employeeName: 'John Doe',
    leaveTypeId: 1, leaveTypeName: 'Annual Leave',
    plannedFromDate: '2026-04-01', plannedToDate: '2026-04-05',
    numberOfDays: 5, notes: 'Vacation', status: 'PLANNED',
    createdAt: '2026-03-01T00:00:00', updatedAt: '2026-03-01T00:00:00'
  };

  const mockPage: Page<LeavePlanResponse> = {
    content: [mockRecord],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    leavePlanServiceSpy = jasmine.createSpyObj('LeavePlanService',
      ['getAll', 'getByStatus', 'cancel', 'convertToApplication']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    leavePlanServiceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        LeavePlanListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule,
        RouterTestingModule
      ],
      providers: [
        { provide: LeavePlanService, useValue: leavePlanServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LeavePlanListComponent);
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

  it('should load records on init', () => {
    component.ngOnInit();
    expect(leavePlanServiceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle record load error', () => {
    leavePlanServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadRecords();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load leave plans');
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

  it('should clear filters', () => {
    component.selectedStatus = 'PLANNED';
    component.clearFilters();
    expect(component.selectedStatus).toBe('');
  });

  it('should filter by status', () => {
    leavePlanServiceSpy.getByStatus.and.returnValue(of(mockPage));
    component.selectedStatus = 'PLANNED';
    component.loadRecords();
    expect(leavePlanServiceSpy.getByStatus).toHaveBeenCalledWith('PLANNED', 0, 10);
  });

  it('should cancel a plan', () => {
    const cancelledRecord = { ...mockRecord, status: 'CANCELLED' as const };
    leavePlanServiceSpy.cancel.and.returnValue(of(cancelledRecord));
    component.cancelPlan(1);
    expect(leavePlanServiceSpy.cancel).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Leave plan cancelled successfully');
  });

  it('should handle cancel error', () => {
    leavePlanServiceSpy.cancel.and.returnValue(throwError(() => new Error('fail')));
    component.cancelPlan(1);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to cancel leave plan');
  });

  it('should convert a plan', () => {
    const convertedRecord = { ...mockRecord, status: 'CONVERTED' as const };
    leavePlanServiceSpy.convertToApplication.and.returnValue(of(convertedRecord));
    component.convertPlan(1);
    expect(leavePlanServiceSpy.convertToApplication).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Leave plan converted to application successfully');
  });

  it('should handle convert error', () => {
    leavePlanServiceSpy.convertToApplication.and.returnValue(throwError(() => new Error('fail')));
    component.convertPlan(1);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to convert leave plan');
  });
});
