import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { LeaveTransactionHistoryComponent } from './leave-transaction-history.component';
import { LeaveBalanceService } from '../../../services/leave-balance.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { LeaveTransactionResponse } from '../../../models/leave-balance.model';

describe('LeaveTransactionHistoryComponent', () => {
  let component: LeaveTransactionHistoryComponent;
  let fixture: ComponentFixture<LeaveTransactionHistoryComponent>;
  let leaveBalanceServiceSpy: jasmine.SpyObj<LeaveBalanceService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  const mockTransaction: LeaveTransactionResponse = {
    id: 1, employeeId: 1, employeeCode: 'EMP001', employeeName: 'John Doe',
    leaveTypeId: 1, leaveTypeName: 'Casual Leave',
    transactionType: 'CREDIT', amount: 12, balanceAfter: 12,
    referenceType: null, referenceId: null,
    description: 'Annual credit', createdBy: 'Admin',
    createdAt: '2025-01-01T00:00:00'
  };

  const mockPage: Page<LeaveTransactionResponse> = {
    content: [mockTransaction],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    leaveBalanceServiceSpy = jasmine.createSpyObj('LeaveBalanceService', ['getTransactions']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    await TestBed.configureTestingModule({
      imports: [
        LeaveTransactionHistoryComponent,
        HttpClientTestingModule,
        NoopAnimationsModule,
        RouterTestingModule
      ],
      providers: [
        { provide: LeaveBalanceService, useValue: leaveBalanceServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LeaveTransactionHistoryComponent);
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

  it('should not load records without employee ID', () => {
    component.loadRecords();
    expect(leaveBalanceServiceSpy.getTransactions).not.toHaveBeenCalled();
  });

  it('should load records for employee', () => {
    leaveBalanceServiceSpy.getTransactions.and.returnValue(of(mockPage));
    component.selectedEmployeeId = 1;
    component.loadRecords();
    expect(leaveBalanceServiceSpy.getTransactions).toHaveBeenCalledWith(1, 0, 10, undefined, undefined);
    expect(component.dataSource.data.length).toBe(1);
    expect(component.loading).toBeFalse();
  });

  it('should handle load error', () => {
    leaveBalanceServiceSpy.getTransactions.and.returnValue(throwError(() => new Error('fail')));
    component.selectedEmployeeId = 1;
    component.loadRecords();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load transactions');
    expect(component.loading).toBeFalse();
  });

  it('should filter by transaction type', () => {
    leaveBalanceServiceSpy.getTransactions.and.returnValue(of(mockPage));
    component.selectedEmployeeId = 1;
    component.selectedTransactionType = 'CREDIT';
    component.loadRecords();
    expect(leaveBalanceServiceSpy.getTransactions).toHaveBeenCalledWith(1, 0, 10, undefined, 'CREDIT');
  });

  it('should reset page index on search', () => {
    leaveBalanceServiceSpy.getTransactions.and.returnValue(of(mockPage));
    component.selectedEmployeeId = 1;
    component.pageIndex = 5;
    component.onSearch();
    expect(component.pageIndex).toBe(0);
  });

  it('should update pagination on page change', () => {
    leaveBalanceServiceSpy.getTransactions.and.returnValue(of(mockPage));
    component.selectedEmployeeId = 1;
    component.onPageChange({ pageIndex: 2, pageSize: 25, length: 100 });
    expect(component.pageIndex).toBe(2);
    expect(component.pageSize).toBe(25);
  });

  it('should clear filters', () => {
    leaveBalanceServiceSpy.getTransactions.and.returnValue(of(mockPage));
    component.selectedEmployeeId = 1;
    component.selectedTransactionType = 'CREDIT';
    component.clearFilters();
    expect(component.selectedTransactionType).toBe('');
  });

  it('should return positive class for positive amount', () => {
    expect(component.getAmountClass(5)).toBe('positive');
  });

  it('should return negative class for negative amount', () => {
    expect(component.getAmountClass(-3)).toBe('negative');
  });
});
