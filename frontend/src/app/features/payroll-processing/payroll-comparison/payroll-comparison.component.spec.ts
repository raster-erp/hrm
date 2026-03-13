import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { PayrollComparisonComponent } from './payroll-comparison.component';
import { PayrollService } from '../../../services/payroll.service';
import { NotificationService } from '../../../services/notification.service';
import { PayrollRunResponse, PayrollDetailResponse } from '../../../models/payroll.model';
import { Page } from '../../../models/page.model';

describe('PayrollComparisonComponent', () => {
  let component: PayrollComparisonComponent;
  let fixture: ComponentFixture<PayrollComparisonComponent>;
  let payrollServiceSpy: jasmine.SpyObj<PayrollService>;
  let notificationSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockCurrentRun: PayrollRunResponse = {
    id: 2, periodYear: 2026, periodMonth: 2,
    runDate: '2026-02-28', status: 'COMPUTED',
    totalGross: 600000, totalDeductions: 60000, totalNet: 540000,
    employeeCount: 10, notes: 'February payroll',
    createdAt: '2026-02-25T00:00:00', updatedAt: '2026-02-25T00:00:00'
  };

  const mockPreviousRun: PayrollRunResponse = {
    id: 1, periodYear: 2026, periodMonth: 1,
    runDate: '2026-01-31', status: 'FINALIZED',
    totalGross: 500000, totalDeductions: 50000, totalNet: 450000,
    employeeCount: 10, notes: 'January payroll',
    createdAt: '2026-01-25T00:00:00', updatedAt: '2026-01-25T00:00:00'
  };

  const mockAllRunsPage: Page<PayrollRunResponse> = {
    content: [mockCurrentRun, mockPreviousRun],
    totalElements: 2, totalPages: 1, size: 1000, number: 0,
    first: true, last: true, empty: false
  };

  const mockCurrentDetails: Page<PayrollDetailResponse> = {
    content: [
      {
        id: 1, payrollRunId: 2, employeeId: 1,
        employeeName: 'John Doe', employeeCode: 'EMP001',
        salaryStructureId: 1, salaryStructureName: 'Standard',
        basicSalary: 50000, grossSalary: 80000,
        totalDeductions: 10000, netSalary: 70000,
        componentBreakup: '[]', daysPayable: 28, lopDays: 0,
        createdAt: '2026-02-25T00:00:00', updatedAt: '2026-02-25T00:00:00'
      }
    ],
    totalElements: 1, totalPages: 1, size: 1000, number: 0,
    first: true, last: true, empty: false
  };

  const mockPreviousDetails: Page<PayrollDetailResponse> = {
    content: [
      {
        id: 2, payrollRunId: 1, employeeId: 1,
        employeeName: 'John Doe', employeeCode: 'EMP001',
        salaryStructureId: 1, salaryStructureName: 'Standard',
        basicSalary: 50000, grossSalary: 75000,
        totalDeductions: 10000, netSalary: 65000,
        componentBreakup: '[]', daysPayable: 30, lopDays: 0,
        createdAt: '2026-01-25T00:00:00', updatedAt: '2026-01-25T00:00:00'
      }
    ],
    totalElements: 1, totalPages: 1, size: 1000, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    payrollServiceSpy = jasmine.createSpyObj('PayrollService', [
      'getRunById', 'getAllRuns', 'getDetails'
    ]);
    notificationSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    payrollServiceSpy.getRunById.and.returnValue(of(mockCurrentRun));
    payrollServiceSpy.getAllRuns.and.returnValue(of(mockAllRunsPage));
    payrollServiceSpy.getDetails.and.callFake((runId: number) => {
      if (runId === 2) return of(mockCurrentDetails);
      return of(mockPreviousDetails);
    });

    await TestBed.configureTestingModule({
      imports: [
        PayrollComparisonComponent,
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
            params: of({ id: '2' }),
            snapshot: { paramMap: { get: (key: string) => '2' } }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PayrollComparisonComponent);
    component = fixture.componentInstance;
    component.ngOnInit();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load comparison data on init', () => {
    expect(payrollServiceSpy.getRunById).toHaveBeenCalledWith(2);
    expect(payrollServiceSpy.getAllRuns).toHaveBeenCalledWith(0, 1000);
    expect(payrollServiceSpy.getDetails).toHaveBeenCalledWith(2, 0, 1000);
    expect(payrollServiceSpy.getDetails).toHaveBeenCalledWith(1, 0, 1000);
    expect(component.currentRun).toEqual(mockCurrentRun);
    expect(component.previousRun).toEqual(mockPreviousRun);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should compute correct variance', () => {
    const row = component.dataSource.data[0];
    expect(row.grossVariance).toBe(5000);
    expect(row.netVariance).toBe(5000);
    expect(row.currentGross).toBe(80000);
    expect(row.previousGross).toBe(75000);
  });

  it('should handle no previous run', () => {
    const noMatchPage: Page<PayrollRunResponse> = {
      content: [mockCurrentRun],
      totalElements: 1, totalPages: 1, size: 1000, number: 0,
      first: true, last: true, empty: false
    };
    payrollServiceSpy.getAllRuns.and.returnValue(of(noMatchPage));
    component.loadComparison();
    expect(component.noPreviousRun).toBeTrue();
  });

  it('should handle load run error', () => {
    payrollServiceSpy.getRunById.and.returnValue(throwError(() => new Error('fail')));
    component.loadComparison();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to load payroll run');
  });

  it('should handle getAllRuns error', () => {
    payrollServiceSpy.getRunById.and.returnValue(of(mockCurrentRun));
    payrollServiceSpy.getAllRuns.and.returnValue(throwError(() => new Error('fail')));
    component.loadComparison();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to load payroll runs');
  });

  it('should handle getDetails error', () => {
    payrollServiceSpy.getDetails.and.returnValue(throwError(() => new Error('fail')));
    component.loadDetails(2, 1);
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to load payroll details');
  });

  it('should format period correctly', () => {
    expect(component.formatPeriod(mockCurrentRun)).toBe('February 2026');
    expect(component.formatPeriod(mockPreviousRun)).toBe('January 2026');
    expect(component.formatPeriod(null)).toBe('');
  });

  it('should return correct variance class', () => {
    expect(component.getVarianceClass(100)).toBe('variance-positive');
    expect(component.getVarianceClass(-100)).toBe('variance-negative');
    expect(component.getVarianceClass(0)).toBe('');
  });

  it('should handle January to December year boundary', () => {
    const janRun: PayrollRunResponse = {
      ...mockCurrentRun, id: 3, periodYear: 2026, periodMonth: 1
    };
    const decRun: PayrollRunResponse = {
      ...mockPreviousRun, id: 4, periodYear: 2025, periodMonth: 12
    };
    const runsPage: Page<PayrollRunResponse> = {
      content: [janRun, decRun],
      totalElements: 2, totalPages: 1, size: 1000, number: 0,
      first: true, last: true, empty: false
    };

    payrollServiceSpy.getRunById.and.returnValue(of(janRun));
    payrollServiceSpy.getAllRuns.and.returnValue(of(runsPage));
    component.loadComparison();
    expect(component.previousRun).toEqual(decRun);
  });

  it('should handle employee in current but not previous run', () => {
    const emptyPrevDetails: Page<PayrollDetailResponse> = {
      content: [],
      totalElements: 0, totalPages: 0, size: 1000, number: 0,
      first: true, last: true, empty: true
    };
    payrollServiceSpy.getDetails.and.callFake((runId: number) => {
      if (runId === 2) return of(mockCurrentDetails);
      return of(emptyPrevDetails);
    });
    component.loadDetails(2, 1);
    const row = component.dataSource.data[0];
    expect(row.previousGross).toBe(0);
    expect(row.previousNet).toBe(0);
    expect(row.grossVariance).toBe(80000);
  });

  it('should navigate back', () => {
    component.goBack();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/payroll-processing']);
  });
});
