import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { OvertimeRecordListComponent } from './overtime-record-list.component';
import { OvertimeRecordService } from '../../../services/overtime-record.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { OvertimeRecordResponse } from '../../../models/overtime-record.model';

describe('OvertimeRecordListComponent', () => {
  let component: OvertimeRecordListComponent;
  let fixture: ComponentFixture<OvertimeRecordListComponent>;
  let overtimeRecordServiceSpy: jasmine.SpyObj<OvertimeRecordService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockRecord: OvertimeRecordResponse = {
    id: 1, employeeId: 100, employeeCode: 'EMP001', employeeName: 'John Doe',
    overtimeDate: '2026-01-15', overtimePolicyId: 1, overtimePolicyName: 'Standard OT',
    overtimePolicyType: 'REGULAR', overtimeMinutes: 60, status: 'PENDING',
    source: 'MANUAL', shiftStartTime: '09:00', shiftEndTime: '17:00',
    actualStartTime: null, actualEndTime: null, remarks: 'Test',
    approvedBy: null, approvedAt: null,
    createdAt: '2026-01-15T00:00:00', updatedAt: '2026-01-15T00:00:00'
  };

  const mockPage: Page<OvertimeRecordResponse> = {
    content: [mockRecord],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    overtimeRecordServiceSpy = jasmine.createSpyObj('OvertimeRecordService',
      ['getAll', 'getByStatus', 'approve', 'delete']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    overtimeRecordServiceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        OvertimeRecordListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: OvertimeRecordService, useValue: overtimeRecordServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: Router, useValue: routerSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({}),
            snapshot: { paramMap: { get: () => null } }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(OvertimeRecordListComponent);
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
    expect(overtimeRecordServiceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle record load error', () => {
    overtimeRecordServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadRecords();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load overtime records');
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

  it('should clear status filter', () => {
    component.selectedStatus = 'PENDING';
    component.clearFilters();
    expect(component.selectedStatus).toBe('');
  });

  it('should navigate to edit page', () => {
    component.editRecord(mockRecord);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/overtime-records', 1, 'edit']);
  });

  it('should filter by status', () => {
    overtimeRecordServiceSpy.getByStatus.and.returnValue(of(mockPage));
    component.selectedStatus = 'PENDING';
    component.loadRecords();
    expect(overtimeRecordServiceSpy.getByStatus).toHaveBeenCalledWith('PENDING', 0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle status filter error', () => {
    overtimeRecordServiceSpy.getByStatus.and.returnValue(throwError(() => new Error('fail')));
    component.selectedStatus = 'PENDING';
    component.loadRecords();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load overtime records');
  });

  it('should approve record', () => {
    overtimeRecordServiceSpy.approve.and.returnValue(of(mockRecord));
    component.approveRecord(mockRecord);
    expect(overtimeRecordServiceSpy.approve).toHaveBeenCalledWith(1, {
      status: 'APPROVED', approvedBy: 'admin', remarks: ''
    });
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Overtime record approved successfully');
  });

  it('should handle approve error', () => {
    overtimeRecordServiceSpy.approve.and.returnValue(throwError(() => new Error('fail')));
    component.approveRecord(mockRecord);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to approve overtime record');
  });

  it('should reject record', () => {
    overtimeRecordServiceSpy.approve.and.returnValue(of(mockRecord));
    component.rejectRecord(mockRecord);
    expect(overtimeRecordServiceSpy.approve).toHaveBeenCalledWith(1, {
      status: 'REJECTED', approvedBy: 'admin', remarks: ''
    });
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Overtime record rejected successfully');
  });

  it('should handle reject error', () => {
    overtimeRecordServiceSpy.approve.and.returnValue(throwError(() => new Error('fail')));
    component.rejectRecord(mockRecord);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to reject overtime record');
  });

  it('should delete record after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    overtimeRecordServiceSpy.delete.and.returnValue(of(void 0));
    component.deleteRecord(mockRecord);
    expect(overtimeRecordServiceSpy.delete).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalled();
  });

  it('should not delete record if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteRecord(mockRecord);
    expect(overtimeRecordServiceSpy.delete).not.toHaveBeenCalled();
  });

  it('should handle delete error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    overtimeRecordServiceSpy.delete.and.returnValue(throwError(() => new Error('fail')));
    component.deleteRecord(mockRecord);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to delete overtime record');
  });
});
