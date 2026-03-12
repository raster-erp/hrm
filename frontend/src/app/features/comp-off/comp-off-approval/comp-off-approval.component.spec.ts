import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { CompOffApprovalComponent } from './comp-off-approval.component';
import { CompOffService } from '../../../services/comp-off.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { CompOffCreditResponse } from '../../../models/comp-off.model';

describe('CompOffApprovalComponent', () => {
  let component: CompOffApprovalComponent;
  let fixture: ComponentFixture<CompOffApprovalComponent>;
  let compOffServiceSpy: jasmine.SpyObj<CompOffService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  const mockRecord: CompOffCreditResponse = {
    id: 1, employeeId: 100, employeeCode: 'EMP001', employeeName: 'John Doe',
    workedDate: '2026-01-10', reason: 'Worked on holiday',
    creditDate: '2026-01-10', expiryDate: '2026-04-10',
    hoursWorked: 8, status: 'PENDING',
    approvedBy: null, approvedAt: null, usedDate: null,
    remarks: null, createdAt: '2026-01-10T00:00:00', updatedAt: '2026-01-10T00:00:00'
  };

  const mockRecord2: CompOffCreditResponse = {
    ...mockRecord, id: 2, employeeName: 'Jane Smith'
  };

  const mockPage: Page<CompOffCreditResponse> = {
    content: [mockRecord, mockRecord2],
    totalElements: 2, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    compOffServiceSpy = jasmine.createSpyObj('CompOffService',
      ['getByStatus', 'approve']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    compOffServiceSpy.getByStatus.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        CompOffApprovalComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: CompOffService, useValue: compOffServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CompOffApprovalComponent);
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
    expect(compOffServiceSpy.getByStatus).toHaveBeenCalledWith('PENDING', 0, 10);
    expect(component.dataSource.data.length).toBe(2);
  });

  it('should handle load error', () => {
    compOffServiceSpy.getByStatus.and.returnValue(throwError(() => new Error('fail')));
    component.loadRecords();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load pending comp-off requests');
  });

  it('should update pagination on page change', () => {
    component.onPageChange({ pageIndex: 2, pageSize: 25, length: 100 });
    expect(component.pageIndex).toBe(2);
    expect(component.pageSize).toBe(25);
  });

  it('should approve individual request', () => {
    compOffServiceSpy.approve.and.returnValue(of(mockRecord));
    component.approveRequest(mockRecord);
    expect(compOffServiceSpy.approve).toHaveBeenCalledWith(1, {
      status: 'APPROVED', approvedBy: 'admin', remarks: ''
    });
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Comp-off request approved successfully');
  });

  it('should handle approve error', () => {
    compOffServiceSpy.approve.and.returnValue(throwError(() => new Error('fail')));
    component.approveRequest(mockRecord);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to approve comp-off request');
  });

  it('should reject individual request', () => {
    compOffServiceSpy.approve.and.returnValue(of(mockRecord));
    component.rejectRequest(mockRecord);
    expect(compOffServiceSpy.approve).toHaveBeenCalledWith(1, {
      status: 'REJECTED', approvedBy: 'admin', remarks: ''
    });
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Comp-off request rejected successfully');
  });

  it('should handle reject error', () => {
    compOffServiceSpy.approve.and.returnValue(throwError(() => new Error('fail')));
    component.rejectRequest(mockRecord);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to reject comp-off request');
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
    compOffServiceSpy.approve.and.returnValue(of(mockRecord));
    component.ngOnInit();
    component.selection.select(mockRecord, mockRecord2);
    component.bulkApprove();
    expect(compOffServiceSpy.approve).toHaveBeenCalledTimes(2);
  });

  it('should bulk reject selected requests', () => {
    compOffServiceSpy.approve.and.returnValue(of(mockRecord));
    component.ngOnInit();
    component.selection.select(mockRecord, mockRecord2);
    component.bulkReject();
    expect(compOffServiceSpy.approve).toHaveBeenCalledTimes(2);
  });

  it('should clear selection on load', () => {
    component.selection.select(mockRecord);
    component.loadRecords();
    expect(component.selection.selected.length).toBe(0);
  });
});
