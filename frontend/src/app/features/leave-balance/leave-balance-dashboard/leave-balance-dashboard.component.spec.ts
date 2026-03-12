import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { LeaveBalanceDashboardComponent } from './leave-balance-dashboard.component';
import { LeaveBalanceService } from '../../../services/leave-balance.service';
import { NotificationService } from '../../../services/notification.service';
import { LeaveBalanceResponse } from '../../../models/leave-balance.model';

describe('LeaveBalanceDashboardComponent', () => {
  let component: LeaveBalanceDashboardComponent;
  let fixture: ComponentFixture<LeaveBalanceDashboardComponent>;
  let leaveBalanceServiceSpy: jasmine.SpyObj<LeaveBalanceService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  const mockBalance: LeaveBalanceResponse = {
    id: 1, employeeId: 1, employeeCode: 'EMP001', employeeName: 'John Doe',
    leaveTypeId: 1, leaveTypeName: 'Casual Leave', leaveTypeCode: 'CL',
    year: 2025, credited: 12, used: 3, pending: 2, available: 7,
    carryForwarded: 0,
    createdAt: '2025-01-01T00:00:00', updatedAt: '2025-01-01T00:00:00'
  };

  beforeEach(async () => {
    leaveBalanceServiceSpy = jasmine.createSpyObj('LeaveBalanceService',
      ['getBalancesByEmployee']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    await TestBed.configureTestingModule({
      imports: [
        LeaveBalanceDashboardComponent,
        HttpClientTestingModule,
        NoopAnimationsModule,
        RouterTestingModule
      ],
      providers: [
        { provide: LeaveBalanceService, useValue: leaveBalanceServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LeaveBalanceDashboardComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have default year set to current year', () => {
    expect(component.selectedYear).toBe(new Date().getFullYear());
  });

  it('should not load balances without employee ID', () => {
    component.loadBalances();
    expect(leaveBalanceServiceSpy.getBalancesByEmployee).not.toHaveBeenCalled();
  });

  it('should load balances for employee', () => {
    leaveBalanceServiceSpy.getBalancesByEmployee.and.returnValue(of([mockBalance]));
    component.selectedEmployeeId = 1;
    component.loadBalances();
    expect(leaveBalanceServiceSpy.getBalancesByEmployee).toHaveBeenCalledWith(1, component.selectedYear);
    expect(component.balances.length).toBe(1);
    expect(component.loading).toBeFalse();
  });

  it('should handle load error', () => {
    leaveBalanceServiceSpy.getBalancesByEmployee.and.returnValue(throwError(() => new Error('fail')));
    component.selectedEmployeeId = 1;
    component.loadBalances();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load leave balances');
    expect(component.loading).toBeFalse();
  });

  it('should call loadBalances on search', () => {
    leaveBalanceServiceSpy.getBalancesByEmployee.and.returnValue(of([mockBalance]));
    component.selectedEmployeeId = 1;
    component.onSearch();
    expect(leaveBalanceServiceSpy.getBalancesByEmployee).toHaveBeenCalled();
  });

  it('should calculate usage percentage', () => {
    expect(component.getUsagePercentage(mockBalance)).toBe(25);
  });

  it('should return 0 for zero credited', () => {
    const zeroBalance = { ...mockBalance, credited: 0, carryForwarded: 0 };
    expect(component.getUsagePercentage(zeroBalance)).toBe(0);
  });
});
