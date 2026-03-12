import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { YearEndProcessingComponent } from './year-end-processing.component';
import { LeaveBalanceService } from '../../../services/leave-balance.service';
import { NotificationService } from '../../../services/notification.service';
import { YearEndSummaryResponse } from '../../../models/leave-balance.model';

describe('YearEndProcessingComponent', () => {
  let component: YearEndProcessingComponent;
  let fixture: ComponentFixture<YearEndProcessingComponent>;
  let leaveBalanceServiceSpy: jasmine.SpyObj<LeaveBalanceService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  const mockSummary: YearEndSummaryResponse = {
    processedYear: 2025, nextYear: 2026,
    employeesProcessed: 10, balancesCreated: 8,
    totalCarryForwarded: 40, totalLapsed: 15
  };

  beforeEach(async () => {
    leaveBalanceServiceSpy = jasmine.createSpyObj('LeaveBalanceService', ['processYearEnd']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    await TestBed.configureTestingModule({
      imports: [
        YearEndProcessingComponent,
        HttpClientTestingModule,
        NoopAnimationsModule,
        RouterTestingModule
      ],
      providers: [
        { provide: LeaveBalanceService, useValue: leaveBalanceServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(YearEndProcessingComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have default year set to previous year', () => {
    expect(component.selectedYear).toBe(new Date().getFullYear() - 1);
  });

  it('should show error if year is not set', () => {
    component.selectedYear = 0;
    component.processYearEnd();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Please enter a year');
  });

  it('should process year end on confirm', () => {
    leaveBalanceServiceSpy.processYearEnd.and.returnValue(of(mockSummary));
    spyOn(window, 'confirm').and.returnValue(true);

    component.selectedYear = 2025;
    component.processedBy = 'Admin';
    component.processYearEnd();

    expect(leaveBalanceServiceSpy.processYearEnd).toHaveBeenCalledWith({
      year: 2025,
      processedBy: 'Admin'
    });
    expect(component.summary).toEqual(mockSummary);
    expect(component.processing).toBeFalse();
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Year-end processing completed successfully');
  });

  it('should not process if user cancels confirm', () => {
    spyOn(window, 'confirm').and.returnValue(false);

    component.selectedYear = 2025;
    component.processYearEnd();

    expect(leaveBalanceServiceSpy.processYearEnd).not.toHaveBeenCalled();
  });

  it('should handle processing error', () => {
    leaveBalanceServiceSpy.processYearEnd.and.returnValue(throwError(() => new Error('fail')));
    spyOn(window, 'confirm').and.returnValue(true);

    component.selectedYear = 2025;
    component.processYearEnd();

    expect(component.processing).toBeFalse();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Year-end processing failed');
  });
});
