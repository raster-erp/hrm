import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { PayrollDetailComponent } from './payroll-detail.component';
import { PayrollService } from '../../../services/payroll.service';
import { NotificationService } from '../../../services/notification.service';
import { PayrollRunResponse, PayrollDetailResponse } from '../../../models/payroll.model';
import { Page } from '../../../models/page.model';

describe('PayrollDetailComponent', () => {
  let component: PayrollDetailComponent;
  let fixture: ComponentFixture<PayrollDetailComponent>;
  let payrollServiceSpy: jasmine.SpyObj<PayrollService>;
  let notificationSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockRun: PayrollRunResponse = {
    id: 1, periodYear: 2026, periodMonth: 1,
    runDate: '2026-01-31', status: 'COMPUTED',
    totalGross: 500000, totalDeductions: 50000, totalNet: 450000,
    employeeCount: 10, notes: 'January payroll',
    createdAt: '2026-01-25T00:00:00', updatedAt: '2026-01-25T00:00:00'
  };

  const mockDetailsPage: Page<PayrollDetailResponse> = {
    content: [
      {
        id: 1, payrollRunId: 1, employeeId: 1,
        employeeName: 'John Doe', employeeCode: 'EMP001',
        salaryStructureId: 1, salaryStructureName: 'Standard',
        basicSalary: 50000, grossSalary: 75000,
        totalDeductions: 10000, netSalary: 65000,
        componentBreakup: JSON.stringify([
          { name: 'Basic', type: 'EARNING', amount: 50000 },
          { name: 'HRA', type: 'EARNING', amount: 25000 }
        ]),
        daysPayable: 30, lopDays: 0,
        createdAt: '2026-01-25T00:00:00', updatedAt: '2026-01-25T00:00:00'
      }
    ],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    payrollServiceSpy = jasmine.createSpyObj('PayrollService', [
      'getRunById', 'getDetails', 'computePayroll', 'verifyRun', 'finalizeRun', 'reverseRun'
    ]);
    notificationSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    payrollServiceSpy.getRunById.and.returnValue(of(mockRun));
    payrollServiceSpy.getDetails.and.returnValue(of(mockDetailsPage));

    await TestBed.configureTestingModule({
      imports: [
        PayrollDetailComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: PayrollService, useValue: payrollServiceSpy },
        { provide: NotificationService, useValue: notificationSpy },
        { provide: Router, useValue: routerSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({ id: '1' }),
            snapshot: { paramMap: { get: (key: string) => '1' } }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PayrollDetailComponent);
    component = fixture.componentInstance;
    component.ngOnInit();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load run and details on init', () => {
    component.ngOnInit();
    expect(payrollServiceSpy.getRunById).toHaveBeenCalledWith(1);
    expect(payrollServiceSpy.getDetails).toHaveBeenCalledWith(1, 0, 10);
    expect(component.run).toEqual(mockRun);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle load run error', () => {
    payrollServiceSpy.getRunById.and.returnValue(throwError(() => new Error('fail')));
    component.loadRun();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to load payroll run');
  });

  it('should handle load details error', () => {
    payrollServiceSpy.getDetails.and.returnValue(throwError(() => new Error('fail')));
    component.loadDetails();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to load payroll details');
  });

  it('should update pagination on page change', () => {
    component.onPageChange({ pageIndex: 1, pageSize: 25, length: 50 });
    expect(component.pageIndex).toBe(1);
    expect(component.pageSize).toBe(25);
  });

  it('should format period correctly', () => {
    component.run = mockRun;
    expect(component.formatPeriod()).toBe('January 2026');
  });

  it('should return empty string for period when no run', () => {
    component.run = null;
    expect(component.formatPeriod()).toBe('');
  });

  it('should return correct status class', () => {
    expect(component.getStatusClass('COMPUTED')).toBe('status-computed');
    expect(component.getStatusClass('UNKNOWN')).toBe('status-draft');
  });

  it('should detect variance', () => {
    const detail = mockDetailsPage.content[0];
    expect(component.hasVariance(detail)).toBeFalse();

    const highVarianceDetail = { ...detail, netSalary: 150000 };
    expect(component.hasVariance(highVarianceDetail)).toBeTrue();
  });

  it('should not detect variance for zero basic salary', () => {
    const detail = { ...mockDetailsPage.content[0], basicSalary: 0 };
    expect(component.hasVariance(detail)).toBeFalse();
  });

  it('should view breakup', () => {
    const detail = mockDetailsPage.content[0];
    component.viewBreakup(detail);
    expect(component.selectedDetail).toEqual(detail);
    expect(component.breakupItems.length).toBe(2);
  });

  it('should handle invalid breakup JSON', () => {
    const detail = { ...mockDetailsPage.content[0], componentBreakup: 'invalid-json' };
    component.viewBreakup(detail);
    expect(component.breakupItems.length).toBe(0);
  });

  it('should handle empty breakup', () => {
    const detail = { ...mockDetailsPage.content[0], componentBreakup: '' };
    component.viewBreakup(detail);
    expect(component.breakupItems.length).toBe(0);
  });

  it('should close breakup', () => {
    component.selectedDetail = mockDetailsPage.content[0];
    component.closeBreakup();
    expect(component.selectedDetail).toBeNull();
    expect(component.breakupItems.length).toBe(0);
  });

  it('should compute payroll', () => {
    payrollServiceSpy.computePayroll.and.returnValue(of(mockRun));
    component.computePayroll();
    expect(payrollServiceSpy.computePayroll).toHaveBeenCalledWith(1);
    expect(notificationSpy.success).toHaveBeenCalledWith('Payroll computation started');
  });

  it('should handle compute error', () => {
    payrollServiceSpy.computePayroll.and.returnValue(throwError(() => new Error('fail')));
    component.computePayroll();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to compute payroll');
  });

  it('should verify payroll', () => {
    payrollServiceSpy.verifyRun.and.returnValue(of(mockRun));
    component.verifyRun();
    expect(payrollServiceSpy.verifyRun).toHaveBeenCalledWith(1);
    expect(notificationSpy.success).toHaveBeenCalledWith('Payroll verified successfully');
  });

  it('should handle verify error', () => {
    payrollServiceSpy.verifyRun.and.returnValue(throwError(() => new Error('fail')));
    component.verifyRun();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to verify payroll');
  });

  it('should finalize payroll after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    payrollServiceSpy.finalizeRun.and.returnValue(of(mockRun));
    component.finalizeRun();
    expect(payrollServiceSpy.finalizeRun).toHaveBeenCalledWith(1);
    expect(notificationSpy.success).toHaveBeenCalledWith('Payroll finalized successfully');
  });

  it('should not finalize if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.finalizeRun();
    expect(payrollServiceSpy.finalizeRun).not.toHaveBeenCalled();
  });

  it('should handle finalize error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    payrollServiceSpy.finalizeRun.and.returnValue(throwError(() => new Error('fail')));
    component.finalizeRun();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to finalize payroll');
  });

  it('should reverse payroll after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    payrollServiceSpy.reverseRun.and.returnValue(of(mockRun));
    component.reverseRun();
    expect(payrollServiceSpy.reverseRun).toHaveBeenCalledWith(1);
    expect(notificationSpy.success).toHaveBeenCalledWith('Payroll reversed successfully');
  });

  it('should not reverse if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.reverseRun();
    expect(payrollServiceSpy.reverseRun).not.toHaveBeenCalled();
  });

  it('should handle reverse error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    payrollServiceSpy.reverseRun.and.returnValue(throwError(() => new Error('fail')));
    component.reverseRun();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to reverse payroll');
  });

  it('should navigate back', () => {
    component.goBack();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/payroll-processing']);
  });
});
