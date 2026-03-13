import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { PayrollAdjustmentComponent } from './payroll-adjustment.component';
import { PayrollService } from '../../../services/payroll.service';
import { NotificationService } from '../../../services/notification.service';
import { PayrollAdjustmentResponse } from '../../../models/payroll.model';

describe('PayrollAdjustmentComponent', () => {
  let component: PayrollAdjustmentComponent;
  let fixture: ComponentFixture<PayrollAdjustmentComponent>;
  let payrollServiceSpy: jasmine.SpyObj<PayrollService>;
  let notificationSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockAdjustments: PayrollAdjustmentResponse[] = [
    {
      id: 1, payrollRunId: 1, employeeId: 1,
      employeeName: 'John Doe', employeeCode: 'EMP001',
      adjustmentType: 'ADDITION', componentName: 'Bonus',
      amount: 5000, reason: 'Performance bonus',
      createdAt: '2026-01-25T00:00:00'
    }
  ];

  const mockNewAdjustment: PayrollAdjustmentResponse = {
    id: 2, payrollRunId: 1, employeeId: 2,
    employeeName: 'Jane Smith', employeeCode: 'EMP002',
    adjustmentType: 'DEDUCTION', componentName: 'Penalty',
    amount: 1000, reason: 'Late submission',
    createdAt: '2026-01-26T00:00:00'
  };

  beforeEach(async () => {
    payrollServiceSpy = jasmine.createSpyObj('PayrollService', [
      'getAdjustments', 'createAdjustment', 'deleteAdjustment'
    ]);
    notificationSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    payrollServiceSpy.getAdjustments.and.returnValue(of(mockAdjustments));

    await TestBed.configureTestingModule({
      imports: [
        PayrollAdjustmentComponent,
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

    fixture = TestBed.createComponent(PayrollAdjustmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form group', () => {
    expect(component.adjustmentForm).toBeDefined();
  });

  it('should set runId from route params', () => {
    expect(component.runId).toBe(1);
  });

  it('should load adjustments on init', () => {
    expect(payrollServiceSpy.getAdjustments).toHaveBeenCalledWith(1);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle load adjustments error', () => {
    payrollServiceSpy.getAdjustments.and.returnValue(throwError(() => new Error('fail')));
    component.loadAdjustments();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to load adjustments');
  });

  it('should require employeeId', () => {
    expect(component.adjustmentForm.get('employeeId')?.hasError('required')).toBeTrue();
  });

  it('should require adjustmentType', () => {
    expect(component.adjustmentForm.get('adjustmentType')?.hasError('required')).toBeTrue();
  });

  it('should require componentName', () => {
    expect(component.adjustmentForm.get('componentName')?.hasError('required')).toBeTrue();
  });

  it('should require amount', () => {
    expect(component.adjustmentForm.get('amount')?.hasError('required')).toBeTrue();
  });

  it('should require reason', () => {
    expect(component.adjustmentForm.get('reason')?.hasError('required')).toBeTrue();
  });

  it('should validate minimum amount', () => {
    component.adjustmentForm.patchValue({ amount: 0 });
    expect(component.adjustmentForm.get('amount')?.hasError('min')).toBeTrue();
  });

  it('should show error if submitting invalid form', () => {
    component.onSubmit();
    expect(notificationSpy.error).toHaveBeenCalledWith('Please fill in all required fields correctly');
  });

  it('should create adjustment on valid submit', () => {
    fillValidForm();
    payrollServiceSpy.createAdjustment.and.returnValue(of(mockNewAdjustment));
    component.onSubmit();
    expect(payrollServiceSpy.createAdjustment).toHaveBeenCalled();
    expect(notificationSpy.success).toHaveBeenCalledWith('Adjustment added successfully');
  });

  it('should include runId in adjustment request', () => {
    fillValidForm();
    payrollServiceSpy.createAdjustment.and.returnValue(of(mockNewAdjustment));
    component.onSubmit();
    const request = payrollServiceSpy.createAdjustment.calls.mostRecent().args[0];
    expect(request.payrollRunId).toBe(1);
  });

  it('should handle create error', () => {
    fillValidForm();
    payrollServiceSpy.createAdjustment.and.returnValue(throwError(() => new Error('fail')));
    component.onSubmit();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to add adjustment');
  });

  it('should delete adjustment after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    payrollServiceSpy.deleteAdjustment.and.returnValue(of(void 0));
    component.deleteAdjustment(mockAdjustments[0]);
    expect(payrollServiceSpy.deleteAdjustment).toHaveBeenCalledWith(1);
    expect(notificationSpy.success).toHaveBeenCalledWith('Adjustment deleted successfully');
  });

  it('should not delete if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteAdjustment(mockAdjustments[0]);
    expect(payrollServiceSpy.deleteAdjustment).not.toHaveBeenCalled();
  });

  it('should handle delete error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    payrollServiceSpy.deleteAdjustment.and.returnValue(throwError(() => new Error('fail')));
    component.deleteAdjustment(mockAdjustments[0]);
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to delete adjustment');
  });

  it('should navigate back to run detail', () => {
    component.goBack();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/payroll-processing', 1]);
  });

  it('should have two adjustment types', () => {
    expect(component.adjustmentTypes.length).toBe(2);
  });

  it('should reset saving flag on error', () => {
    fillValidForm();
    payrollServiceSpy.createAdjustment.and.returnValue(throwError(() => new Error('fail')));
    component.onSubmit();
    expect(component.saving).toBeFalse();
  });

  function fillValidForm() {
    component.adjustmentForm.patchValue({
      employeeId: 2,
      adjustmentType: 'DEDUCTION',
      componentName: 'Penalty',
      amount: 1000,
      reason: 'Late submission'
    });
  }
});
