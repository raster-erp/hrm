import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { DeviationListComponent } from './deviation-list.component';
import { AttendanceDeviationService } from '../../../services/attendance-deviation.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { AttendanceDeviationResponse } from '../../../models/attendance-deviation.model';

describe('DeviationListComponent', () => {
  let component: DeviationListComponent;
  let fixture: ComponentFixture<DeviationListComponent>;
  let deviationServiceSpy: jasmine.SpyObj<AttendanceDeviationService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  const mockRecord: AttendanceDeviationResponse = {
    id: 1, employeeId: 100, employeeCode: 'EMP001', employeeName: 'John Doe',
    deviationDate: '2026-01-15', type: 'LATE_COMING', deviationMinutes: 15,
    scheduledTime: '09:00', actualTime: '09:15', gracePeriodMinutes: 5,
    penaltyAction: 'WARNING', status: 'PENDING', remarks: 'Test',
    approvedBy: null, approvedAt: null,
    createdAt: '2026-01-15T00:00:00', updatedAt: '2026-01-15T00:00:00'
  };

  const mockPage: Page<AttendanceDeviationResponse> = {
    content: [mockRecord],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    deviationServiceSpy = jasmine.createSpyObj('AttendanceDeviationService',
      ['getAll', 'getByType', 'getByStatus', 'approve', 'delete']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    deviationServiceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        DeviationListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: AttendanceDeviationService, useValue: deviationServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DeviationListComponent);
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
    expect(deviationServiceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle record load error', () => {
    deviationServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadRecords();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load attendance deviations');
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
    component.selectedType = 'LATE_COMING';
    component.selectedStatus = 'PENDING';
    component.clearFilters();
    expect(component.selectedType).toBe('');
    expect(component.selectedStatus).toBe('');
  });

  it('should filter by type', () => {
    deviationServiceSpy.getByType.and.returnValue(of(mockPage));
    component.selectedType = 'LATE_COMING';
    component.loadRecords();
    expect(deviationServiceSpy.getByType).toHaveBeenCalledWith('LATE_COMING', 0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should filter by status', () => {
    deviationServiceSpy.getByStatus.and.returnValue(of(mockPage));
    component.selectedStatus = 'PENDING';
    component.loadRecords();
    expect(deviationServiceSpy.getByStatus).toHaveBeenCalledWith('PENDING', 0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should approve deviation', () => {
    deviationServiceSpy.approve.and.returnValue(of(mockRecord));
    component.approveDeviation(mockRecord);
    expect(deviationServiceSpy.approve).toHaveBeenCalledWith(1, {
      status: 'APPROVED', approvedBy: 'admin', remarks: ''
    });
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Deviation approved successfully');
  });

  it('should handle approve error', () => {
    deviationServiceSpy.approve.and.returnValue(throwError(() => new Error('fail')));
    component.approveDeviation(mockRecord);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to approve deviation');
  });

  it('should waive deviation', () => {
    deviationServiceSpy.approve.and.returnValue(of(mockRecord));
    component.waiveDeviation(mockRecord);
    expect(deviationServiceSpy.approve).toHaveBeenCalledWith(1, {
      status: 'WAIVED', approvedBy: 'admin', remarks: ''
    });
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Deviation waived successfully');
  });

  it('should handle waive error', () => {
    deviationServiceSpy.approve.and.returnValue(throwError(() => new Error('fail')));
    component.waiveDeviation(mockRecord);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to waive deviation');
  });

  it('should delete deviation after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    deviationServiceSpy.delete.and.returnValue(of(void 0));
    component.deleteDeviation(mockRecord);
    expect(deviationServiceSpy.delete).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalled();
  });

  it('should not delete if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteDeviation(mockRecord);
    expect(deviationServiceSpy.delete).not.toHaveBeenCalled();
  });

  it('should handle delete error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    deviationServiceSpy.delete.and.returnValue(throwError(() => new Error('fail')));
    component.deleteDeviation(mockRecord);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to delete deviation');
  });
});
