import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { TaxComputationSummaryComponent } from './tax-computation-summary.component';
import { TaxComputationService } from '../../../services/tax-computation.service';
import { NotificationService } from '../../../services/notification.service';
import { TaxComputationResponse, Form16DataResponse } from '../../../models/tax-computation.model';

describe('TaxComputationSummaryComponent', () => {
  let component: TaxComputationSummaryComponent;
  let fixture: ComponentFixture<TaxComputationSummaryComponent>;
  let serviceSpy: jasmine.SpyObj<TaxComputationService>;
  let notificationSpy: jasmine.SpyObj<NotificationService>;

  const mockComputations: TaxComputationResponse[] = [{
    id: 1, employeeId: 101, employeeName: 'John Doe',
    financialYear: '2025-26', month: 4,
    grossAnnualIncome: 1200000, totalExemptions: 150000,
    taxableIncome: 1050000, totalAnnualTax: 105000,
    monthlyTds: 8750, cess: 4200, surcharge: 0,
    tdsDeductedTillDate: 8750, remainingTds: 96250,
    regime: 'NEW', createdAt: '2026-01-01T00:00:00', updatedAt: '2026-01-01T00:00:00'
  }];

  const mockForm16: Form16DataResponse = {
    employeeId: 101, employeeName: 'John Doe',
    financialYear: '2025-26', regime: 'NEW',
    grossAnnualIncome: 1200000, totalExemptions: 150000,
    taxableIncome: 1050000, totalTaxPayable: 105000,
    totalTdsDeducted: 87500, cess: 4200, surcharge: 0,
    verifiedInvestments: [], monthlyBreakup: []
  };

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('TaxComputationService', [
      'getByEmployeeAndYear', 'computeMonthlyTds', 'generateForm16Data'
    ]);
    notificationSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    serviceSpy.getByEmployeeAndYear.and.returnValue(of(mockComputations));
    serviceSpy.computeMonthlyTds.and.returnValue(of(mockComputations[0]));
    serviceSpy.generateForm16Data.and.returnValue(of(mockForm16));

    await TestBed.configureTestingModule({
      imports: [
        TaxComputationSummaryComponent,
        HttpClientTestingModule,
        NoopAnimationsModule,
        ReactiveFormsModule
      ],
      providers: [
        { provide: TaxComputationService, useValue: serviceSpy },
        { provide: NotificationService, useValue: notificationSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TaxComputationSummaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should search for computations', () => {
    component.searchForm.setValue({ employeeId: 101, financialYear: '2025-26' });
    component.onSearch();
    expect(serviceSpy.getByEmployeeAndYear).toHaveBeenCalledWith(101, '2025-26');
    expect(component.dataSource.data.length).toBe(1);
    expect(component.loading).toBeFalse();
  });

  it('should handle search error', () => {
    serviceSpy.getByEmployeeAndYear.and.returnValue(throwError(() => new Error('fail')));
    component.searchForm.setValue({ employeeId: 101, financialYear: '2025-26' });
    component.onSearch();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to load tax computations');
    expect(component.loading).toBeFalse();
  });

  it('should compute monthly TDS', () => {
    component.computeForm.setValue({ employeeId: 101, financialYear: '2025-26', month: 4 });
    component.onCompute();
    expect(serviceSpy.computeMonthlyTds).toHaveBeenCalledWith({
      employeeId: 101, financialYear: '2025-26', month: 4
    });
    expect(notificationSpy.success).toHaveBeenCalledWith('TDS computed successfully');
    expect(component.computeLoading).toBeFalse();
  });

  it('should handle compute error', () => {
    serviceSpy.computeMonthlyTds.and.returnValue(throwError(() => new Error('fail')));
    component.computeForm.setValue({ employeeId: 101, financialYear: '2025-26', month: 4 });
    component.onCompute();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to compute TDS');
    expect(component.computeLoading).toBeFalse();
  });

  it('should show error when search form is invalid', () => {
    component.searchForm.setValue({ employeeId: null, financialYear: '' });
    component.onSearch();
    expect(notificationSpy.error).toHaveBeenCalledWith('Please enter Employee ID and Financial Year');
    expect(serviceSpy.getByEmployeeAndYear).not.toHaveBeenCalled();
  });

  it('should generate Form 16 data', () => {
    component.searchForm.setValue({ employeeId: 101, financialYear: '2025-26' });
    component.onGenerateForm16();
    expect(serviceSpy.generateForm16Data).toHaveBeenCalledWith(101, '2025-26');
    expect(component.form16Data).toEqual(mockForm16);
    expect(component.form16Loading).toBeFalse();
  });

  it('should handle Form 16 error', () => {
    serviceSpy.generateForm16Data.and.returnValue(throwError(() => new Error('fail')));
    component.searchForm.setValue({ employeeId: 101, financialYear: '2025-26' });
    component.onGenerateForm16();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to generate Form 16 data');
    expect(component.form16Loading).toBeFalse();
  });

  it('should get month name correctly', () => {
    expect(component.getMonthName(1)).toBe('January');
    expect(component.getMonthName(4)).toBe('April');
    expect(component.getMonthName(12)).toBe('December');
    expect(component.getMonthName(13)).toBe('Unknown');
  });

  it('should toggle compute section visibility', () => {
    expect(component.showComputeSection).toBeFalse();
    component.toggleComputeSection();
    expect(component.showComputeSection).toBeTrue();
    component.toggleComputeSection();
    expect(component.showComputeSection).toBeFalse();
  });
});
