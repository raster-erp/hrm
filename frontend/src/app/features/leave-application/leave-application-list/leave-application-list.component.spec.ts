import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { LeaveApplicationListComponent } from './leave-application-list.component';
import { LeaveApplicationService } from '../../../services/leave-application.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { LeaveApplicationResponse } from '../../../models/leave-application.model';

describe('LeaveApplicationListComponent', () => {
  let component: LeaveApplicationListComponent;
  let fixture: ComponentFixture<LeaveApplicationListComponent>;
  let leaveApplicationServiceSpy: jasmine.SpyObj<LeaveApplicationService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  const mockRecord: LeaveApplicationResponse = {
    id: 1, employeeId: 100, employeeCode: 'EMP001', employeeName: 'John Doe',
    leaveTypeId: 1, leaveTypeName: 'Casual Leave',
    fromDate: '2026-01-15', toDate: '2026-01-17', numberOfDays: 3,
    reason: 'Personal work', status: 'PENDING', approvalLevel: 1,
    remarks: null, approvedBy: null, approvedAt: null,
    createdAt: '2026-01-10T00:00:00', updatedAt: '2026-01-10T00:00:00'
  };

  const mockPage: Page<LeaveApplicationResponse> = {
    content: [mockRecord],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    leaveApplicationServiceSpy = jasmine.createSpyObj('LeaveApplicationService',
      ['getAll', 'getByStatus', 'approve', 'cancel', 'delete']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    leaveApplicationServiceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        LeaveApplicationListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule,
        RouterTestingModule
      ],
      providers: [
        { provide: LeaveApplicationService, useValue: leaveApplicationServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LeaveApplicationListComponent);
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
    expect(leaveApplicationServiceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle record load error', () => {
    leaveApplicationServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadRecords();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load leave applications');
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
    component.selectedStatus = 'PENDING';
    component.clearFilters();
    expect(component.selectedStatus).toBe('');
  });

  it('should filter by status', () => {
    leaveApplicationServiceSpy.getByStatus.and.returnValue(of(mockPage));
    component.selectedStatus = 'PENDING';
    component.loadRecords();
    expect(leaveApplicationServiceSpy.getByStatus).toHaveBeenCalledWith('PENDING', 0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should approve request', () => {
    leaveApplicationServiceSpy.approve.and.returnValue(of(mockRecord));
    component.approveRequest(mockRecord);
    expect(leaveApplicationServiceSpy.approve).toHaveBeenCalledWith(1, {
      status: 'APPROVED', approvedBy: 'admin', remarks: ''
    });
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Leave application approved successfully');
  });

  it('should handle approve error', () => {
    leaveApplicationServiceSpy.approve.and.returnValue(throwError(() => new Error('fail')));
    component.approveRequest(mockRecord);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to approve leave application');
  });

  it('should reject request', () => {
    leaveApplicationServiceSpy.approve.and.returnValue(of(mockRecord));
    component.rejectRequest(mockRecord);
    expect(leaveApplicationServiceSpy.approve).toHaveBeenCalledWith(1, {
      status: 'REJECTED', approvedBy: 'admin', remarks: ''
    });
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Leave application rejected successfully');
  });

  it('should handle reject error', () => {
    leaveApplicationServiceSpy.approve.and.returnValue(throwError(() => new Error('fail')));
    component.rejectRequest(mockRecord);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to reject leave application');
  });

  it('should cancel request', () => {
    leaveApplicationServiceSpy.cancel.and.returnValue(of(mockRecord));
    component.cancelRequest(mockRecord);
    expect(leaveApplicationServiceSpy.cancel).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Leave application cancelled successfully');
  });

  it('should handle cancel error', () => {
    leaveApplicationServiceSpy.cancel.and.returnValue(throwError(() => new Error('fail')));
    component.cancelRequest(mockRecord);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to cancel leave application');
  });

  it('should delete request after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    leaveApplicationServiceSpy.delete.and.returnValue(of(void 0));
    component.deleteRequest(mockRecord);
    expect(leaveApplicationServiceSpy.delete).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalled();
  });

  it('should not delete if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteRequest(mockRecord);
    expect(leaveApplicationServiceSpy.delete).not.toHaveBeenCalled();
  });

  it('should handle delete error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    leaveApplicationServiceSpy.delete.and.returnValue(throwError(() => new Error('fail')));
    component.deleteRequest(mockRecord);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to delete leave application');
  });
});
