import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { CompOffFormComponent } from './comp-off-form.component';
import { CompOffService } from '../../../services/comp-off.service';
import { NotificationService } from '../../../services/notification.service';
import { EmployeeService } from '../../../services/employee.service';
import { CompOffCreditResponse } from '../../../models/comp-off.model';
import { Page } from '../../../models/page.model';
import { EmployeeResponse } from '../../../models/employee.model';

describe('CompOffFormComponent', () => {
  let component: CompOffFormComponent;
  let fixture: ComponentFixture<CompOffFormComponent>;
  let compOffServiceSpy: jasmine.SpyObj<CompOffService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let employeeServiceSpy: jasmine.SpyObj<EmployeeService>;

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

  const mockResponse: CompOffCreditResponse = {
    id: 1, employeeId: 100, employeeCode: 'EMP001', employeeName: 'John Doe',
    workedDate: '2026-01-10', reason: 'Worked on holiday',
    creditDate: '2026-01-10', expiryDate: '2026-04-10',
    hoursWorked: 8, status: 'PENDING',
    approvedBy: null, approvedAt: null, usedDate: null,
    remarks: null, createdAt: '2026-01-10T00:00:00', updatedAt: '2026-01-10T00:00:00'
  };

  beforeEach(async () => {
    compOffServiceSpy = jasmine.createSpyObj('CompOffService', ['create']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    employeeServiceSpy = jasmine.createSpyObj('EmployeeService', ['getAll']);

    employeeServiceSpy.getAll.and.returnValue(of(mockEmployeePage));

    await TestBed.configureTestingModule({
      imports: [
        CompOffFormComponent,
        HttpClientTestingModule,
        NoopAnimationsModule,
        RouterTestingModule
      ],
      providers: [
        { provide: CompOffService, useValue: compOffServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: EmployeeService, useValue: employeeServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CompOffFormComponent);
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

  it('should handle employee load error', () => {
    employeeServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.ngOnInit();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load employees');
  });

  it('should not submit invalid form', () => {
    component.ngOnInit();
    component.onSubmit();
    expect(compOffServiceSpy.create).not.toHaveBeenCalled();
  });

  it('should submit valid form', () => {
    compOffServiceSpy.create.and.returnValue(of(mockResponse));
    component.ngOnInit();
    component.form.patchValue({
      employeeId: 100,
      workedDate: '2026-01-10',
      reason: 'Worked on holiday'
    });
    component.onSubmit();
    expect(compOffServiceSpy.create).toHaveBeenCalled();
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Comp-off request submitted successfully');
  });

  it('should handle create error', () => {
    compOffServiceSpy.create.and.returnValue(throwError(() => new Error('fail')));
    component.ngOnInit();
    component.form.patchValue({
      employeeId: 100,
      workedDate: '2026-01-10',
      reason: 'Worked on holiday'
    });
    component.onSubmit();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to submit comp-off request');
  });

  it('should have required validators', () => {
    component.ngOnInit();
    expect(component.form.get('employeeId')?.hasError('required')).toBeTrue();
    expect(component.form.get('workedDate')?.hasError('required')).toBeTrue();
    expect(component.form.get('reason')?.hasError('required')).toBeTrue();
  });
});
