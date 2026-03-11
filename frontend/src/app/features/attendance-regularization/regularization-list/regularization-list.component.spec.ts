import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { RegularizationListComponent } from './regularization-list.component';
import { RegularizationRequestService } from '../../../services/regularization-request.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { RegularizationRequestResponse } from '../../../models/regularization-request.model';

describe('RegularizationListComponent', () => {
  let component: RegularizationListComponent;
  let fixture: ComponentFixture<RegularizationListComponent>;
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

  const mockPage: Page<RegularizationRequestResponse> = {
    content: [mockRecord],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    regularizationServiceSpy = jasmine.createSpyObj('RegularizationRequestService',
      ['getAll', 'getByType', 'getByStatus', 'approve', 'delete']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    regularizationServiceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        RegularizationListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule,
        RouterTestingModule
      ],
      providers: [
        { provide: RegularizationRequestService, useValue: regularizationServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegularizationListComponent);
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
    expect(regularizationServiceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle record load error', () => {
    regularizationServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadRecords();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load regularization requests');
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
    component.selectedType = 'MISSED_PUNCH';
    component.selectedStatus = 'PENDING';
    component.clearFilters();
    expect(component.selectedType).toBe('');
    expect(component.selectedStatus).toBe('');
  });

  it('should filter by type', () => {
    regularizationServiceSpy.getByType.and.returnValue(of(mockPage));
    component.selectedType = 'MISSED_PUNCH';
    component.loadRecords();
    expect(regularizationServiceSpy.getByType).toHaveBeenCalledWith('MISSED_PUNCH', 0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should filter by status', () => {
    regularizationServiceSpy.getByStatus.and.returnValue(of(mockPage));
    component.selectedStatus = 'PENDING';
    component.loadRecords();
    expect(regularizationServiceSpy.getByStatus).toHaveBeenCalledWith('PENDING', 0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should approve request', () => {
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

  it('should reject request', () => {
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

  it('should delete request after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    regularizationServiceSpy.delete.and.returnValue(of(void 0));
    component.deleteRequest(mockRecord);
    expect(regularizationServiceSpy.delete).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalled();
  });

  it('should not delete if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteRequest(mockRecord);
    expect(regularizationServiceSpy.delete).not.toHaveBeenCalled();
  });

  it('should handle delete error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    regularizationServiceSpy.delete.and.returnValue(throwError(() => new Error('fail')));
    component.deleteRequest(mockRecord);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to delete request');
  });

  it('should format type correctly', () => {
    expect(component.formatType('MISSED_PUNCH')).toBe('Missed Punch');
    expect(component.formatType('ON_DUTY')).toBe('On Duty');
    expect(component.formatType('CLIENT_VISIT')).toBe('Client Visit');
    expect(component.formatType('UNKNOWN')).toBe('UNKNOWN');
  });
});
