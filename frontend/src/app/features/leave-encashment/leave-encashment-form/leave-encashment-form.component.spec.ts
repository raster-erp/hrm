import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { LeaveEncashmentFormComponent } from './leave-encashment-form.component';
import { LeaveEncashmentService } from '../../../services/leave-encashment.service';
import { NotificationService } from '../../../services/notification.service';
import { EmployeeService } from '../../../services/employee.service';
import { LeaveTypeService } from '../../../services/leave-type.service';
import { EncashmentEligibilityResponse, LeaveEncashmentResponse } from '../../../models/leave-encashment.model';
import { Page } from '../../../models/page.model';
import { EmployeeResponse } from '../../../models/employee.model';
import { LeaveTypeResponse } from '../../../models/leave-type.model';

describe('LeaveEncashmentFormComponent', () => {
  let component: LeaveEncashmentFormComponent;
  let fixture: ComponentFixture<LeaveEncashmentFormComponent>;
  let leaveEncashmentServiceSpy: jasmine.SpyObj<LeaveEncashmentService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let employeeServiceSpy: jasmine.SpyObj<EmployeeService>;
  let leaveTypeServiceSpy: jasmine.SpyObj<LeaveTypeService>;

  const mockEmployee: EmployeeResponse = {
    id: 100, employeeCode: 'EMP001', firstName: 'John', lastName: 'Doe',
    email: 'john@test.com', phone: '1234567890', gender: 'Male',
    dateOfBirth: '1990-01-01', joiningDate: '2020-01-01',
    address: '', city: '', state: '', country: '', zipCode: '',
    emergencyContactName: '', emergencyContactPhone: '',
    bankName: '', bankAccountNumber: '', bankIfscCode: '',
    departmentId: 1, departmentName: 'IT',
    designationId: 1, designationName: 'Developer',
    employmentStatus: 'ACTIVE', photoUrl: '', createdAt: '', updatedAt: ''
  };

  const mockEmployeePage: Page<EmployeeResponse> = {
    content: [mockEmployee],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  const mockLeaveType: LeaveTypeResponse = {
    id: 1, name: 'Earned Leave', code: 'EL', description: 'Earned Leave',
    category: 'EARNED', active: true,
    createdAt: '', updatedAt: ''
  };

  const mockEligibility: EncashmentEligibilityResponse = {
    employeeId: 100, employeeName: 'John Doe',
    leaveTypeId: 1, leaveTypeName: 'Earned Leave',
    year: 2026, eligible: true,
    availableBalance: 20, minRequiredBalance: 10,
    maxEncashableDays: 10, perDaySalary: 2000,
    reason: ''
  };

  const mockNotEligible: EncashmentEligibilityResponse = {
    ...mockEligibility, eligible: false, reason: 'Insufficient balance'
  };

  const mockResponse: LeaveEncashmentResponse = {
    id: 1, employeeId: 100, employeeCode: 'EMP001', employeeName: 'John Doe',
    leaveTypeId: 1, leaveTypeName: 'Earned Leave',
    year: 2026, numberOfDays: 5, perDaySalary: 2000, totalAmount: 10000,
    status: 'PENDING', approvedBy: null, approvedAt: null,
    remarks: null, createdAt: '2026-01-10T00:00:00', updatedAt: '2026-01-10T00:00:00'
  };

  beforeEach(async () => {
    leaveEncashmentServiceSpy = jasmine.createSpyObj('LeaveEncashmentService',
      ['checkEligibility', 'create']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    employeeServiceSpy = jasmine.createSpyObj('EmployeeService', ['getAll']);
    leaveTypeServiceSpy = jasmine.createSpyObj('LeaveTypeService', ['getActive']);

    employeeServiceSpy.getAll.and.returnValue(of(mockEmployeePage));
    leaveTypeServiceSpy.getActive.and.returnValue(of([mockLeaveType]));

    await TestBed.configureTestingModule({
      imports: [
        LeaveEncashmentFormComponent,
        HttpClientTestingModule,
        NoopAnimationsModule,
        RouterTestingModule
      ],
      providers: [
        { provide: LeaveEncashmentService, useValue: leaveEncashmentServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: EmployeeService, useValue: employeeServiceSpy },
        { provide: LeaveTypeService, useValue: leaveTypeServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LeaveEncashmentFormComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form on init', () => {
    component.ngOnInit();
    expect(component.form).toBeTruthy();
    expect(component.form.get('year')?.value).toBe(new Date().getFullYear());
  });

  it('should load employees on init', () => {
    component.ngOnInit();
    expect(employeeServiceSpy.getAll).toHaveBeenCalled();
    expect(component.employees.length).toBe(1);
  });

  it('should load leave types on init', () => {
    component.ngOnInit();
    expect(leaveTypeServiceSpy.getActive).toHaveBeenCalled();
    expect(component.leaveTypes.length).toBe(1);
  });

  it('should handle employee load error', () => {
    employeeServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.ngOnInit();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load employees');
  });

  it('should handle leave type load error', () => {
    leaveTypeServiceSpy.getActive.and.returnValue(throwError(() => new Error('fail')));
    component.ngOnInit();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load leave types');
  });

  it('should require employee and leave type for eligibility check', () => {
    component.ngOnInit();
    component.checkEligibility();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Please select employee, leave type, and year');
  });

  it('should check eligibility successfully', () => {
    leaveEncashmentServiceSpy.checkEligibility.and.returnValue(of(mockEligibility));
    component.ngOnInit();
    component.form.patchValue({ employeeId: 100, leaveTypeId: 1, year: 2026 });
    component.checkEligibility();
    expect(leaveEncashmentServiceSpy.checkEligibility).toHaveBeenCalledWith(100, 1, 2026);
    expect(component.eligibility).toEqual(mockEligibility);
    expect(component.form.get('numberOfDays')?.enabled).toBeTrue();
  });

  it('should handle not eligible result', () => {
    leaveEncashmentServiceSpy.checkEligibility.and.returnValue(of(mockNotEligible));
    component.ngOnInit();
    component.form.patchValue({ employeeId: 100, leaveTypeId: 1, year: 2026 });
    component.checkEligibility();
    expect(component.eligibility?.eligible).toBeFalse();
    expect(component.form.get('numberOfDays')?.disabled).toBeTrue();
  });

  it('should handle eligibility check error', () => {
    leaveEncashmentServiceSpy.checkEligibility.and.returnValue(throwError(() => new Error('fail')));
    component.ngOnInit();
    component.form.patchValue({ employeeId: 100, leaveTypeId: 1, year: 2026 });
    component.checkEligibility();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to check eligibility');
  });

  it('should calculate amount on days change', () => {
    component.ngOnInit();
    component.eligibility = mockEligibility;
    component.form.get('numberOfDays')?.enable();
    component.form.get('numberOfDays')?.setValue(5);
    component.onDaysChange();
    expect(component.calculatedAmount).toBe(10000);
  });

  it('should reset amount when days is cleared', () => {
    component.ngOnInit();
    component.eligibility = mockEligibility;
    component.form.get('numberOfDays')?.enable();
    component.form.get('numberOfDays')?.setValue(null);
    component.onDaysChange();
    expect(component.calculatedAmount).toBe(0);
  });

  it('should not submit invalid form', () => {
    component.ngOnInit();
    component.onSubmit();
    expect(leaveEncashmentServiceSpy.create).not.toHaveBeenCalled();
  });

  it('should submit valid form', () => {
    leaveEncashmentServiceSpy.create.and.returnValue(of(mockResponse));
    component.ngOnInit();
    component.eligibility = mockEligibility;
    component.form.get('numberOfDays')?.enable();
    component.form.patchValue({
      employeeId: 100,
      leaveTypeId: 1,
      year: 2026,
      numberOfDays: 5
    });
    component.onSubmit();
    expect(leaveEncashmentServiceSpy.create).toHaveBeenCalled();
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Encashment request submitted successfully');
  });

  it('should handle create error', () => {
    leaveEncashmentServiceSpy.create.and.returnValue(throwError(() => new Error('fail')));
    component.ngOnInit();
    component.eligibility = mockEligibility;
    component.form.get('numberOfDays')?.enable();
    component.form.patchValue({
      employeeId: 100,
      leaveTypeId: 1,
      year: 2026,
      numberOfDays: 5
    });
    component.onSubmit();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to submit encashment request');
  });

  it('should have required validators', () => {
    component.ngOnInit();
    expect(component.form.get('employeeId')?.hasError('required')).toBeTrue();
    expect(component.form.get('leaveTypeId')?.hasError('required')).toBeTrue();
  });
});
