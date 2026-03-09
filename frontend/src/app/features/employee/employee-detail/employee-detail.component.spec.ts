import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { EmployeeDetailComponent } from './employee-detail.component';
import { EmployeeService } from '../../../services/employee.service';
import { NotificationService } from '../../../services/notification.service';
import { EmployeeResponse } from '../../../models/employee.model';

describe('EmployeeDetailComponent', () => {
  let component: EmployeeDetailComponent;
  let fixture: ComponentFixture<EmployeeDetailComponent>;
  let employeeServiceSpy: jasmine.SpyObj<EmployeeService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockEmployee: EmployeeResponse = {
    id: 1, employeeCode: 'EMP001', firstName: 'John', lastName: 'Doe',
    email: 'john@example.com', phone: '1234567890', dateOfBirth: '1990-01-01',
    gender: 'MALE', address: '123 St', city: 'City', state: 'State',
    country: 'Country', zipCode: '12345', emergencyContactName: 'Jane',
    emergencyContactPhone: '0987654321', bankName: 'Bank', bankAccountNumber: '111',
    bankIfscCode: 'IFSC001', departmentId: 1, departmentName: 'Engineering',
    designationId: 1, designationName: 'Developer', joiningDate: '2023-01-01',
    employmentStatus: 'ACTIVE', photoUrl: '', createdAt: '', updatedAt: ''
  };

  beforeEach(async () => {
    employeeServiceSpy = jasmine.createSpyObj('EmployeeService', ['getById', 'getDocuments']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    employeeServiceSpy.getById.and.returnValue(of(mockEmployee));
    employeeServiceSpy.getDocuments.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [
        EmployeeDetailComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: EmployeeService, useValue: employeeServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: Router, useValue: routerSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({ id: '1' }),
            snapshot: { paramMap: { get: () => '1' } }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EmployeeDetailComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load employee on init', () => {
    component.ngOnInit();
    expect(employeeServiceSpy.getById).toHaveBeenCalledWith(1);
    expect(component.employee).toEqual(mockEmployee);
    expect(component.loading).toBeFalse();
  });

  it('should load documents on init', () => {
    component.ngOnInit();
    expect(employeeServiceSpy.getDocuments).toHaveBeenCalledWith(1);
    expect(component.documentsLoading).toBeFalse();
  });

  it('should handle employee load error', () => {
    employeeServiceSpy.getById.and.returnValue(throwError(() => new Error('fail')));
    component.loadEmployee(1);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load employee details');
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/employees']);
  });

  it('should handle documents load error', () => {
    employeeServiceSpy.getDocuments.and.returnValue(throwError(() => new Error('fail')));
    component.loadDocuments(1);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load documents');
  });

  it('should navigate to edit page', () => {
    component.employee = mockEmployee;
    component.editEmployee();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/employees', 1, 'edit']);
  });

  it('should not navigate to edit if no employee', () => {
    component.employee = null;
    component.editEmployee();
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });

  it('should navigate back to list', () => {
    component.goBack();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/employees']);
  });

  it('should format file size correctly', () => {
    expect(component.formatFileSize(0)).toBe('0 Bytes');
    expect(component.formatFileSize(1024)).toBe('1 KB');
    expect(component.formatFileSize(1048576)).toBe('1 MB');
    expect(component.formatFileSize(1073741824)).toBe('1 GB');
  });

  it('should have document columns defined', () => {
    expect(component.documentColumns.length).toBe(4);
  });

  it('should return correct status color', () => {
    expect(component.getStatusColor('ACTIVE')).toBe('primary');
    expect(component.getStatusColor('INACTIVE')).toBe('warn');
    expect(component.getStatusColor('ON_LEAVE')).toBe('accent');
    expect(component.getStatusColor('TERMINATED')).toBe('warn');
    expect(component.getStatusColor('UNKNOWN')).toBe('');
  });

  it('should redirect if no id in route', () => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      imports: [EmployeeDetailComponent, HttpClientTestingModule, NoopAnimationsModule],
      providers: [
        { provide: EmployeeService, useValue: employeeServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: { params: of({}), snapshot: { paramMap: { get: () => null } } } }
      ]
    });
    const fix = TestBed.createComponent(EmployeeDetailComponent);
    fix.componentInstance.ngOnInit();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/employees']);
  });
});
