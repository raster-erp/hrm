import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { RegularizationApprovalComponent } from './regularization-approval.component';
import { RegularizationRequestService } from '../../../services/regularization-request.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { RegularizationRequestResponse } from '../../../models/regularization-request.model';

describe('RegularizationApprovalComponent', () => {
  let component: RegularizationApprovalComponent;
  let fixture: ComponentFixture<RegularizationApprovalComponent>;
  let regularizationServiceSpy: jasmine.SpyObj<RegularizationRequestService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  const mockRecord: RegularizationRequestResponse = {
    id: 1, employeeId: 100, employeeCode: 'EMP001', employeeName: 'John Doe',
    requestDate: '2026-01-15', type: 'MISSED_PUNCH', reason: 'Forgot to punch',
    originalPunchIn: null, originalPunchOut: null,
    correctedPunchIn: '2026-01-15T09:00:00', correctedPunchOut: '2026-01-15T18:00:00',
    status: 'PENDING', approvalLevel: 1, remarks: null,
    approvedBy: null, approvedAt: null,
    createdAt: '2026-01-15T00:00:00', updatedAt: '2026-01-15T00:00:00'
  };

  const mockRecord2: RegularizationRequestResponse = {
    ...mockRecord, id: 2, employeeName: 'Jane Smith'
  };

  const mockPage: Page<RegularizationRequestResponse> = {
    content: [mockRecord, mockRecord2],
    totalElements: 2, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    regularizationServiceSpy = jasmine.createSpyObj('RegularizationRequestService',
      ['getByStatus', 'approve']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    regularizationServiceSpy.getByStatus.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        RegularizationApprovalComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: RegularizationRequestService, useValue: regularizationServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegularizationApprovalComponent);
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
    expect(regularizationServiceSpy.getByStatus).toHaveBeenCalledWith('PENDING', 0, 10);
    expect(component.dataSource.data.length).toBe(2);
  });

  it('should handle load error', () => {
    regularizationServiceSpy.getByStatus.and.returnValue(throwError(() => new Error('fail')));
    component.loadRecords();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load pending requests');
  });

  it('should update pagination on page change', () => {
    component.onPageChange({ pageIndex: 2, pageSize: 25, length: 100 });
    expect(component.pageIndex).toBe(2);
    expect(component.pageSize).toBe(25);
  });

  it('should approve individual request', () => {
    regularizationServiceSpy.approve.and.returnValue(of(mockRecord));
    component.approveRequest(mockRecord);
    expect(regularizationServiceSpy.approve).toHaveBeenCalledWith(1, {
      status: 'APPROVED', approvedBy: 'admin', remarks: ''
    });
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Request approved successfully');
  });

  it('should handle approve error', () => {
    regularizationServiceSpy.approve.and.returnValue(throwError(() => new Error('fail')));
    component.approveRequest(mockRecord);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to approve request');
  });

  it('should reject individual request', () => {
    regularizationServiceSpy.approve.and.returnValue(of(mockRecord));
    component.rejectRequest(mockRecord);
    expect(regularizationServiceSpy.approve).toHaveBeenCalledWith(1, {
      status: 'REJECTED', approvedBy: 'admin', remarks: ''
    });
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Request rejected successfully');
  });

  it('should handle reject error', () => {
    regularizationServiceSpy.approve.and.returnValue(throwError(() => new Error('fail')));
    component.rejectRequest(mockRecord);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to reject request');
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
    regularizationServiceSpy.approve.and.returnValue(of(mockRecord));
    component.ngOnInit();
    component.selection.select(mockRecord, mockRecord2);
    component.bulkApprove();
    expect(regularizationServiceSpy.approve).toHaveBeenCalledTimes(2);
  });

  it('should bulk reject selected requests', () => {
    regularizationServiceSpy.approve.and.returnValue(of(mockRecord));
    component.ngOnInit();
    component.selection.select(mockRecord, mockRecord2);
    component.bulkReject();
    expect(regularizationServiceSpy.approve).toHaveBeenCalledTimes(2);
  });

  it('should format type correctly', () => {
    expect(component.formatType('MISSED_PUNCH')).toBe('Missed Punch');
    expect(component.formatType('ON_DUTY')).toBe('On Duty');
    expect(component.formatType('CLIENT_VISIT')).toBe('Client Visit');
    expect(component.formatType('UNKNOWN')).toBe('UNKNOWN');
  });

  it('should clear selection on load', () => {
    component.selection.select(mockRecord);
    component.loadRecords();
    expect(component.selection.selected.length).toBe(0);
  });
});
