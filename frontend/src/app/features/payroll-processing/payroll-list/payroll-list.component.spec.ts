import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { PayrollListComponent } from './payroll-list.component';
import { PayrollService } from '../../../services/payroll.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { PayrollRunResponse } from '../../../models/payroll.model';

describe('PayrollListComponent', () => {
  let component: PayrollListComponent;
  let fixture: ComponentFixture<PayrollListComponent>;
  let serviceSpy: jasmine.SpyObj<PayrollService>;
  let notificationSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockPage: Page<PayrollRunResponse> = {
    content: [
      {
        id: 1, periodYear: 2026, periodMonth: 1,
        runDate: '2026-01-31', status: 'DRAFT',
        totalGross: 500000, totalDeductions: 50000, totalNet: 450000,
        employeeCount: 10, notes: 'January payroll',
        createdAt: '2026-01-25T00:00:00', updatedAt: '2026-01-25T00:00:00'
      }
    ],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('PayrollService', [
      'getAllRuns', 'computePayroll', 'verifyRun', 'finalizeRun', 'reverseRun'
    ]);
    notificationSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    serviceSpy.getAllRuns.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        PayrollListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: PayrollService, useValue: serviceSpy },
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

    fixture = TestBed.createComponent(PayrollListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have displayed columns defined', () => {
    expect(component.displayedColumns.length).toBeGreaterThan(0);
  });

  it('should load runs on init', () => {
    component.ngOnInit();
    expect(serviceSpy.getAllRuns).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle load error', () => {
    serviceSpy.getAllRuns.and.returnValue(throwError(() => new Error('fail')));
    component.loadRuns();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to load payroll runs');
  });

  it('should update pagination on page change', () => {
    component.onPageChange({ pageIndex: 2, pageSize: 25, length: 100 });
    expect(component.pageIndex).toBe(2);
    expect(component.pageSize).toBe(25);
  });

  it('should format period correctly', () => {
    const run = mockPage.content[0];
    expect(component.formatPeriod(run)).toBe('January 2026');
  });

  it('should return correct status class', () => {
    expect(component.getStatusClass('DRAFT')).toBe('status-draft');
    expect(component.getStatusClass('FINALIZED')).toBe('status-finalized');
    expect(component.getStatusClass('UNKNOWN')).toBe('status-draft');
  });

  it('should navigate to details on viewDetails', () => {
    component.viewDetails(mockPage.content[0]);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/payroll-processing', 1]);
  });

  it('should compute payroll', () => {
    serviceSpy.computePayroll.and.returnValue(of(mockPage.content[0]));
    component.computePayroll(mockPage.content[0]);
    expect(serviceSpy.computePayroll).toHaveBeenCalledWith(1);
    expect(notificationSpy.success).toHaveBeenCalledWith('Payroll computation started');
  });

  it('should handle compute error', () => {
    serviceSpy.computePayroll.and.returnValue(throwError(() => new Error('fail')));
    component.computePayroll(mockPage.content[0]);
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to compute payroll');
  });

  it('should verify payroll run', () => {
    serviceSpy.verifyRun.and.returnValue(of(mockPage.content[0]));
    component.verifyRun(mockPage.content[0]);
    expect(serviceSpy.verifyRun).toHaveBeenCalledWith(1);
    expect(notificationSpy.success).toHaveBeenCalledWith('Payroll verified successfully');
  });

  it('should handle verify error', () => {
    serviceSpy.verifyRun.and.returnValue(throwError(() => new Error('fail')));
    component.verifyRun(mockPage.content[0]);
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to verify payroll');
  });

  it('should finalize payroll run after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    serviceSpy.finalizeRun.and.returnValue(of(mockPage.content[0]));
    component.finalizeRun(mockPage.content[0]);
    expect(serviceSpy.finalizeRun).toHaveBeenCalledWith(1);
    expect(notificationSpy.success).toHaveBeenCalledWith('Payroll finalized successfully');
  });

  it('should not finalize if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.finalizeRun(mockPage.content[0]);
    expect(serviceSpy.finalizeRun).not.toHaveBeenCalled();
  });

  it('should handle finalize error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    serviceSpy.finalizeRun.and.returnValue(throwError(() => new Error('fail')));
    component.finalizeRun(mockPage.content[0]);
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to finalize payroll');
  });

  it('should reverse payroll run after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    serviceSpy.reverseRun.and.returnValue(of(mockPage.content[0]));
    component.reverseRun(mockPage.content[0]);
    expect(serviceSpy.reverseRun).toHaveBeenCalledWith(1);
    expect(notificationSpy.success).toHaveBeenCalledWith('Payroll reversed successfully');
  });

  it('should not reverse if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.reverseRun(mockPage.content[0]);
    expect(serviceSpy.reverseRun).not.toHaveBeenCalled();
  });

  it('should handle reverse error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    serviceSpy.reverseRun.and.returnValue(throwError(() => new Error('fail')));
    component.reverseRun(mockPage.content[0]);
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to reverse payroll');
  });
});
