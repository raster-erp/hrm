import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { PayrollFormComponent } from './payroll-form.component';
import { PayrollService } from '../../../services/payroll.service';
import { NotificationService } from '../../../services/notification.service';
import { PayrollRunResponse } from '../../../models/payroll.model';

describe('PayrollFormComponent', () => {
  let component: PayrollFormComponent;
  let fixture: ComponentFixture<PayrollFormComponent>;
  let payrollServiceSpy: jasmine.SpyObj<PayrollService>;
  let notificationSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockResponse: PayrollRunResponse = {
    id: 1, periodYear: 2026, periodMonth: 1,
    runDate: '2026-01-31', status: 'DRAFT',
    totalGross: 0, totalDeductions: 0, totalNet: 0,
    employeeCount: 0, notes: 'January payroll',
    createdAt: '2026-01-25T00:00:00', updatedAt: '2026-01-25T00:00:00'
  };

  beforeEach(async () => {
    payrollServiceSpy = jasmine.createSpyObj('PayrollService', ['initializeRun']);
    notificationSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [
        PayrollFormComponent,
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
            params: of({}),
            snapshot: { paramMap: { get: () => null } }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PayrollFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form group', () => {
    expect(component.payrollForm).toBeDefined();
  });

  it('should require periodYear', () => {
    component.payrollForm.patchValue({ periodYear: null });
    expect(component.payrollForm.get('periodYear')?.hasError('required')).toBeTrue();
  });

  it('should require periodMonth', () => {
    component.payrollForm.patchValue({ periodMonth: null });
    expect(component.payrollForm.get('periodMonth')?.hasError('required')).toBeTrue();
  });

  it('should validate year range', () => {
    component.payrollForm.patchValue({ periodYear: 1999 });
    expect(component.payrollForm.get('periodYear')?.hasError('min')).toBeTrue();

    component.payrollForm.patchValue({ periodYear: 2101 });
    expect(component.payrollForm.get('periodYear')?.hasError('max')).toBeTrue();
  });

  it('should initialize with current year and month', () => {
    const now = new Date();
    expect(component.payrollForm.get('periodYear')?.value).toBe(now.getFullYear());
    expect(component.payrollForm.get('periodMonth')?.value).toBe(now.getMonth() + 1);
  });

  it('should have 12 months available', () => {
    expect(component.months.length).toBe(12);
  });

  it('should show error if submitting invalid form', () => {
    component.payrollForm.patchValue({ periodYear: null, periodMonth: null });
    component.onSubmit();
    expect(notificationSpy.error).toHaveBeenCalledWith('Please fill in all required fields correctly');
  });

  it('should create payroll run on valid submit', () => {
    payrollServiceSpy.initializeRun.and.returnValue(of(mockResponse));
    component.onSubmit();
    expect(payrollServiceSpy.initializeRun).toHaveBeenCalled();
    expect(notificationSpy.success).toHaveBeenCalledWith('Payroll run initialized successfully');
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/payroll-processing']);
  });

  it('should handle create error', () => {
    payrollServiceSpy.initializeRun.and.returnValue(throwError(() => new Error('fail')));
    component.onSubmit();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to initialize payroll run');
  });

  it('should navigate back on cancel', () => {
    component.onCancel();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/payroll-processing']);
  });

  it('should set saving flag during submit', () => {
    payrollServiceSpy.initializeRun.and.returnValue(of(mockResponse));
    component.onSubmit();
    expect(component.saving).toBeFalse();
  });

  it('should reset saving flag on error', () => {
    payrollServiceSpy.initializeRun.and.returnValue(throwError(() => new Error('fail')));
    component.onSubmit();
    expect(component.saving).toBeFalse();
  });
});
