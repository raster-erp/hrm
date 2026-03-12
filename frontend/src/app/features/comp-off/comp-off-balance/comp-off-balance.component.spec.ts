import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { CompOffBalanceComponent } from './comp-off-balance.component';
import { CompOffService } from '../../../services/comp-off.service';
import { NotificationService } from '../../../services/notification.service';
import { EmployeeService } from '../../../services/employee.service';
import { CompOffBalanceResponse } from '../../../models/comp-off.model';
import { Page } from '../../../models/page.model';
import { EmployeeResponse } from '../../../models/employee.model';

describe('CompOffBalanceComponent', () => {
  let component: CompOffBalanceComponent;
  let fixture: ComponentFixture<CompOffBalanceComponent>;
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

  const mockBalance: CompOffBalanceResponse = {
    employeeId: 100, employeeName: 'John Doe',
    totalCredits: 10, approved: 5, pending: 2,
    used: 2, expired: 1, availableForUse: 5
  };

  beforeEach(async () => {
    compOffServiceSpy = jasmine.createSpyObj('CompOffService', ['getBalance']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    employeeServiceSpy = jasmine.createSpyObj('EmployeeService', ['getAll']);

    employeeServiceSpy.getAll.and.returnValue(of(mockEmployeePage));

    await TestBed.configureTestingModule({
      imports: [
        CompOffBalanceComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: CompOffService, useValue: compOffServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: EmployeeService, useValue: employeeServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CompOffBalanceComponent);
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

  it('should require employee selection for balance', () => {
    component.ngOnInit();
    component.loadBalance();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Please select an employee');
  });

  it('should load balance successfully', () => {
    compOffServiceSpy.getBalance.and.returnValue(of(mockBalance));
    component.ngOnInit();
    component.employeeIdControl.setValue(100);
    component.loadBalance();
    expect(compOffServiceSpy.getBalance).toHaveBeenCalledWith(100);
    expect(component.balance).toEqual(mockBalance);
  });

  it('should handle balance load error', () => {
    compOffServiceSpy.getBalance.and.returnValue(throwError(() => new Error('fail')));
    component.ngOnInit();
    component.employeeIdControl.setValue(100);
    component.loadBalance();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load comp-off balance');
  });
});
