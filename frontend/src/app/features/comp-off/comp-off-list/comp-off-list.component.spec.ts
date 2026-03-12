import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { CompOffListComponent } from './comp-off-list.component';
import { CompOffService } from '../../../services/comp-off.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { CompOffCreditResponse } from '../../../models/comp-off.model';

describe('CompOffListComponent', () => {
  let component: CompOffListComponent;
  let fixture: ComponentFixture<CompOffListComponent>;
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

  const mockApprovedRecord: CompOffCreditResponse = {
    ...mockRecord, id: 2, status: 'APPROVED'
  };

  const mockPage: Page<CompOffCreditResponse> = {
    content: [mockRecord, mockApprovedRecord],
    totalElements: 2, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    compOffServiceSpy = jasmine.createSpyObj('CompOffService',
      ['getAll', 'getByStatus']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    compOffServiceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        CompOffListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule,
        RouterTestingModule
      ],
      providers: [
        { provide: CompOffService, useValue: compOffServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CompOffListComponent);
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
    expect(compOffServiceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(2);
  });

  it('should handle record load error', () => {
    compOffServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadRecords();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load comp-off requests');
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
    compOffServiceSpy.getByStatus.and.returnValue(of(mockPage));
    component.selectedStatus = 'PENDING';
    component.loadRecords();
    expect(compOffServiceSpy.getByStatus).toHaveBeenCalledWith('PENDING', 0, 10);
    expect(component.dataSource.data.length).toBe(2);
  });

  it('should detect expiring soon records', () => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 3);
    const expiringSoon = { ...mockApprovedRecord, expiryDate: tomorrow.toISOString().split('T')[0] };
    expect(component.isExpiringSoon(expiringSoon)).toBeTrue();
  });

  it('should not detect non-approved as expiring', () => {
    expect(component.isExpiringSoon(mockRecord)).toBeFalse();
  });
});
