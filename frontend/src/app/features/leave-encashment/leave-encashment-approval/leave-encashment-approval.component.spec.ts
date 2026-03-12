import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { LeaveEncashmentApprovalComponent } from './leave-encashment-approval.component';
import { LeaveEncashmentService } from '../../../services/leave-encashment.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { LeaveEncashmentResponse } from '../../../models/leave-encashment.model';

describe('LeaveEncashmentApprovalComponent', () => {
  let component: LeaveEncashmentApprovalComponent;
  let fixture: ComponentFixture<LeaveEncashmentApprovalComponent>;
  let leaveEncashmentServiceSpy: jasmine.SpyObj<LeaveEncashmentService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  const mockRecord: LeaveEncashmentResponse = {
    id: 1, employeeId: 100, employeeCode: 'EMP001', employeeName: 'John Doe',
    leaveTypeId: 1, leaveTypeName: 'Earned Leave',
    year: 2026, numberOfDays: 5, perDaySalary: 2000, totalAmount: 10000,
    status: 'PENDING', approvedBy: null, approvedAt: null,
    remarks: null, createdAt: '2026-01-10T00:00:00', updatedAt: '2026-01-10T00:00:00'
  };

  const mockRecord2: LeaveEncashmentResponse = {
    ...mockRecord, id: 2, employeeName: 'Jane Smith'
  };

  const mockPage: Page<LeaveEncashmentResponse> = {
    content: [mockRecord, mockRecord2],
    totalElements: 2, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    leaveEncashmentServiceSpy = jasmine.createSpyObj('LeaveEncashmentService',
      ['getByStatus', 'approve']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    leaveEncashmentServiceSpy.getByStatus.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        LeaveEncashmentApprovalComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: LeaveEncashmentService, useValue: leaveEncashmentServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LeaveEncashmentApprovalComponent);
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
    expect(leaveEncashmentServiceSpy.getByStatus).toHaveBeenCalledWith('PENDING', 0, 10);
    expect(component.dataSource.data.length).toBe(2);
  });

  it('should handle load error', () => {
    leaveEncashmentServiceSpy.getByStatus.and.returnValue(throwError(() => new Error('fail')));
    component.loadRecords();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load pending encashment requests');
  });

  it('should update pagination on page change', () => {
    component.onPageChange({ pageIndex: 2, pageSize: 25, length: 100 });
    expect(component.pageIndex).toBe(2);
    expect(component.pageSize).toBe(25);
  });

  it('should approve individual request', () => {
    leaveEncashmentServiceSpy.approve.and.returnValue(of(mockRecord));
    component.approveRequest(mockRecord);
    expect(leaveEncashmentServiceSpy.approve).toHaveBeenCalledWith(1, {
      status: 'APPROVED', approvedBy: 'admin', remarks: ''
    });
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Encashment request approved successfully');
  });

  it('should handle approve error', () => {
    leaveEncashmentServiceSpy.approve.and.returnValue(throwError(() => new Error('fail')));
    component.approveRequest(mockRecord);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to approve encashment request');
  });

  it('should reject individual request', () => {
    leaveEncashmentServiceSpy.approve.and.returnValue(of(mockRecord));
    component.rejectRequest(mockRecord);
    expect(leaveEncashmentServiceSpy.approve).toHaveBeenCalledWith(1, {
      status: 'REJECTED', approvedBy: 'admin', remarks: ''
    });
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Encashment request rejected successfully');
  });

  it('should handle reject error', () => {
    leaveEncashmentServiceSpy.approve.and.returnValue(throwError(() => new Error('fail')));
    component.rejectRequest(mockRecord);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to reject encashment request');
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
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('No requests selected');
  });

  it('should show error when bulk reject with no selection', () => {
    component.bulkReject();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('No requests selected');
  });

  it('should bulk approve selected requests', () => {
    leaveEncashmentServiceSpy.approve.and.returnValue(of(mockRecord));
    component.ngOnInit();
    component.selection.select(mockRecord, mockRecord2);
    component.bulkApprove();
    expect(leaveEncashmentServiceSpy.approve).toHaveBeenCalledTimes(2);
  });

  it('should bulk reject selected requests', () => {
    leaveEncashmentServiceSpy.approve.and.returnValue(of(mockRecord));
    component.ngOnInit();
    component.selection.select(mockRecord, mockRecord2);
    component.bulkReject();
    expect(leaveEncashmentServiceSpy.approve).toHaveBeenCalledTimes(2);
  });

  it('should clear selection on load', () => {
    component.selection.select(mockRecord);
    component.loadRecords();
    expect(component.selection.selected.length).toBe(0);
  });
});
