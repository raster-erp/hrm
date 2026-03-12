import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { LeavePlanFormComponent } from './leave-plan-form.component';
import { LeavePlanService } from '../../../services/leave-plan.service';
import { NotificationService } from '../../../services/notification.service';
import { EmployeeService } from '../../../services/employee.service';
import { LeaveTypeService } from '../../../services/leave-type.service';
import { Page } from '../../../models/page.model';
import { EmployeeResponse } from '../../../models/employee.model';
import { LeaveTypeResponse } from '../../../models/leave-type.model';
import { LeavePlanResponse } from '../../../models/leave-plan.model';

describe('LeavePlanFormComponent', () => {
  let component: LeavePlanFormComponent;
  let fixture: ComponentFixture<LeavePlanFormComponent>;
  let leavePlanServiceSpy: jasmine.SpyObj<LeavePlanService>;
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
    id: 1, code: 'AL', name: 'Annual Leave', category: 'PAID',
    description: 'Annual leave', active: true,
    createdAt: '', updatedAt: ''
  };

  const mockLeaveTypePage: Page<LeaveTypeResponse> = {
    content: [mockLeaveType],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  const mockResponse: LeavePlanResponse = {
    id: 1, employeeId: 100, employeeCode: 'EMP001', employeeName: 'John Doe',
    leaveTypeId: 1, leaveTypeName: 'Annual Leave',
    plannedFromDate: '2026-04-01', plannedToDate: '2026-04-05',
    numberOfDays: 5, notes: null, status: 'PLANNED',
    createdAt: '2026-03-01T00:00:00', updatedAt: '2026-03-01T00:00:00'
  };

  beforeEach(async () => {
    leavePlanServiceSpy = jasmine.createSpyObj('LeavePlanService', ['create']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    employeeServiceSpy = jasmine.createSpyObj('EmployeeService', ['getAll']);
    leaveTypeServiceSpy = jasmine.createSpyObj('LeaveTypeService', ['getAll']);

    employeeServiceSpy.getAll.and.returnValue(of(mockEmployeePage));
    leaveTypeServiceSpy.getAll.and.returnValue(of(mockLeaveTypePage));

    await TestBed.configureTestingModule({
      imports: [
        LeavePlanFormComponent,
        HttpClientTestingModule,
        NoopAnimationsModule,
        RouterTestingModule
      ],
      providers: [
        { provide: LeavePlanService, useValue: leavePlanServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: EmployeeService, useValue: employeeServiceSpy },
        { provide: LeaveTypeService, useValue: leaveTypeServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LeavePlanFormComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form on init', () => {
    component.ngOnInit();
    expect(component.form).toBeTruthy();
  });

  it('should load employees on init', () => {
    component.ngOnInit();
    expect(employeeServiceSpy.getAll).toHaveBeenCalled();
    expect(component.employees.length).toBe(1);
  });

  it('should load leave types on init', () => {
    component.ngOnInit();
    expect(leaveTypeServiceSpy.getAll).toHaveBeenCalled();
    expect(component.leaveTypes.length).toBe(1);
  });

  it('should handle employee load error', () => {
    employeeServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.ngOnInit();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load employees');
  });

  it('should handle leave type load error', () => {
    leaveTypeServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.ngOnInit();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load leave types');
  });

  it('should not submit invalid form', () => {
    component.ngOnInit();
    component.onSubmit();
    expect(leavePlanServiceSpy.create).not.toHaveBeenCalled();
  });

  it('should submit valid form', () => {
    leavePlanServiceSpy.create.and.returnValue(of(mockResponse));
    component.ngOnInit();
    component.form.patchValue({
      employeeId: 100,
      leaveTypeId: 1,
      plannedFromDate: '2026-04-01',
      plannedToDate: '2026-04-05',
      numberOfDays: 5
    });
    component.onSubmit();
    expect(leavePlanServiceSpy.create).toHaveBeenCalled();
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Leave plan created successfully');
  });

  it('should handle create error', () => {
    leavePlanServiceSpy.create.and.returnValue(throwError(() => new Error('fail')));
    component.ngOnInit();
    component.form.patchValue({
      employeeId: 100,
      leaveTypeId: 1,
      plannedFromDate: '2026-04-01',
      plannedToDate: '2026-04-05',
      numberOfDays: 5
    });
    component.onSubmit();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to create leave plan');
  });

  it('should have required validators', () => {
    component.ngOnInit();
    expect(component.form.get('employeeId')?.hasError('required')).toBeTrue();
    expect(component.form.get('leaveTypeId')?.hasError('required')).toBeTrue();
    expect(component.form.get('plannedFromDate')?.hasError('required')).toBeTrue();
    expect(component.form.get('plannedToDate')?.hasError('required')).toBeTrue();
    expect(component.form.get('numberOfDays')?.hasError('required')).toBeTrue();
  });
});
