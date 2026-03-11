import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { LeaveApprovalComponent } from './leave-approval.component';
import { LeaveApplicationService } from '../../../services/leave-application.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { LeaveApplicationResponse } from '../../../models/leave-application.model';

describe('LeaveApprovalComponent', () => {
  let component: LeaveApprovalComponent;
  let fixture: ComponentFixture<LeaveApprovalComponent>;
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

  const mockRecord2: LeaveApplicationResponse = {
    ...mockRecord, id: 2, employeeName: 'Jane Smith'
  };

  const mockPage: Page<LeaveApplicationResponse> = {
    content: [mockRecord, mockRecord2],
    totalElements: 2, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    leaveApplicationServiceSpy = jasmine.createSpyObj('LeaveApplicationService',
      ['getByStatus', 'approve']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    leaveApplicationServiceSpy.getByStatus.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        LeaveApprovalComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: LeaveApplicationService, useValue: leaveApplicationServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LeaveApprovalComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have displayed columns defined', () => {
    expect(component.displayedColumns.length).toBeGreaterThan(0);
    expect(component.displayedColumns).toContain('select');
  });

  it('should load pending records on init', () => {
    component.ngOnInit();
    expect(leaveApplicationServiceSpy.getByStatus).toHaveBeenCalledWith('PENDING', 0, 10);
    expect(component.dataSource.data.length).toBe(2);
  });

  it('should handle load error', () => {
    leaveApplicationServiceSpy.getByStatus.and.returnValue(throwError(() => new Error('fail')));
    component.loadRecords();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load pending leave applications');
  });

  it('should update pagination on page change', () => {
    component.onPageChange({ pageIndex: 2, pageSize: 25, length: 100 });
    expect(component.pageIndex).toBe(2);
    expect(component.pageSize).toBe(25);
  });

  it('should approve individual request', () => {
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

  it('should reject individual request', () => {
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

  it('should toggle all rows', () => {
    component.ngOnInit();
    component.toggleAllRows();
    expect(component.selection.selected.length).toBe(2);
    component.toggleAllRows();
    expect(component.selection.selected.length).toBe(0);
  });

  it('should check if all selected', () => {
    component.ngOnInit();
    expect(component.isAllSelected()).toBeFalse();
    component.selection.select(...component.dataSource.data);
    expect(component.isAllSelected()).toBeTrue();
  });

  it('should show error when bulk approve with no selection', () => {
    component.bulkApprove();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('No applications selected');
  });

  it('should show error when bulk reject with no selection', () => {
    component.bulkReject();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('No applications selected');
  });

  it('should bulk approve selected requests', () => {
    leaveApplicationServiceSpy.approve.and.returnValue(of(mockRecord));
    component.ngOnInit();
    component.selection.select(mockRecord, mockRecord2);
    component.bulkApprove();
    expect(leaveApplicationServiceSpy.approve).toHaveBeenCalledTimes(2);
  });

  it('should bulk reject selected requests', () => {
    leaveApplicationServiceSpy.approve.and.returnValue(of(mockRecord));
    component.ngOnInit();
    component.selection.select(mockRecord, mockRecord2);
    component.bulkReject();
    expect(leaveApplicationServiceSpy.approve).toHaveBeenCalledTimes(2);
  });

  it('should clear selection on load', () => {
    component.selection.select(mockRecord);
    component.loadRecords();
    expect(component.selection.selected.length).toBe(0);
  });
});
