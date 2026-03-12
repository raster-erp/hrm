import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { LeaveEncashmentListComponent } from './leave-encashment-list.component';
import { LeaveEncashmentService } from '../../../services/leave-encashment.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { LeaveEncashmentResponse } from '../../../models/leave-encashment.model';

describe('LeaveEncashmentListComponent', () => {
  let component: LeaveEncashmentListComponent;
  let fixture: ComponentFixture<LeaveEncashmentListComponent>;
  let leaveEncashmentServiceSpy: jasmine.SpyObj<LeaveEncashmentService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  const mockRecord: LeaveEncashmentResponse = {
    id: 1, employeeId: 100, employeeCode: 'EMP001', employeeName: 'John Doe',
    leaveTypeId: 1, leaveTypeName: 'Earned Leave',
    year: 2026, numberOfDays: 5, perDaySalary: 2000, totalAmount: 10000,
    status: 'PENDING', approvedBy: null, approvedAt: null,
    remarks: null, createdAt: '2026-01-10T00:00:00', updatedAt: '2026-01-10T00:00:00'
  };

  const mockApprovedRecord: LeaveEncashmentResponse = {
    ...mockRecord, id: 2, status: 'APPROVED'
  };

  const mockPage: Page<LeaveEncashmentResponse> = {
    content: [mockRecord, mockApprovedRecord],
    totalElements: 2, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    leaveEncashmentServiceSpy = jasmine.createSpyObj('LeaveEncashmentService',
      ['getAll', 'getByStatus', 'markAsPaid']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    leaveEncashmentServiceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        LeaveEncashmentListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule,
        RouterTestingModule
      ],
      providers: [
        { provide: LeaveEncashmentService, useValue: leaveEncashmentServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LeaveEncashmentListComponent);
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
    expect(leaveEncashmentServiceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(2);
  });

  it('should handle record load error', () => {
    leaveEncashmentServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadRecords();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load leave encashment requests');
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
    leaveEncashmentServiceSpy.getByStatus.and.returnValue(of(mockPage));
    component.selectedStatus = 'PENDING';
    component.loadRecords();
    expect(leaveEncashmentServiceSpy.getByStatus).toHaveBeenCalledWith('PENDING', 0, 10);
    expect(component.dataSource.data.length).toBe(2);
  });

  it('should mark as paid', () => {
    leaveEncashmentServiceSpy.markAsPaid.and.returnValue(of(mockApprovedRecord));
    component.markAsPaid(mockApprovedRecord);
    expect(leaveEncashmentServiceSpy.markAsPaid).toHaveBeenCalledWith(2, 'admin');
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Encashment marked as paid successfully');
  });

  it('should handle mark as paid error', () => {
    leaveEncashmentServiceSpy.markAsPaid.and.returnValue(throwError(() => new Error('fail')));
    component.markAsPaid(mockApprovedRecord);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to mark encashment as paid');
  });
});
