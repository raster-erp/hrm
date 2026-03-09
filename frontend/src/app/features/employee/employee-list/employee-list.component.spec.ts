import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { EmployeeListComponent } from './employee-list.component';
import { EmployeeService } from '../../../services/employee.service';
import { DepartmentService } from '../../../services/department.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { EmployeeResponse } from '../../../models/employee.model';

describe('EmployeeListComponent', () => {
  let component: EmployeeListComponent;
  let fixture: ComponentFixture<EmployeeListComponent>;
  let employeeServiceSpy: jasmine.SpyObj<EmployeeService>;
  let departmentServiceSpy: jasmine.SpyObj<DepartmentService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockPage: Page<EmployeeResponse> = {
    content: [
      {
        id: 1, employeeCode: 'EMP001', firstName: 'John', lastName: 'Doe',
        email: 'john@example.com', phone: '1234567890', dateOfBirth: '1990-01-01',
        gender: 'MALE', address: '123 St', city: 'City', state: 'State',
        country: 'Country', zipCode: '12345', emergencyContactName: 'Jane',
        emergencyContactPhone: '0987654321', bankName: 'Bank', bankAccountNumber: '111',
        bankIfscCode: 'IFSC001', departmentId: 1, departmentName: 'Engineering',
        designationId: 1, designationName: 'Developer', joiningDate: '2023-01-01',
        employmentStatus: 'ACTIVE', photoUrl: '', createdAt: '', updatedAt: ''
      }
    ],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    employeeServiceSpy = jasmine.createSpyObj('EmployeeService', ['getAll', 'search', 'delete']);
    departmentServiceSpy = jasmine.createSpyObj('DepartmentService', ['getAll']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    employeeServiceSpy.getAll.and.returnValue(of(mockPage));
    departmentServiceSpy.getAll.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [
        EmployeeListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: EmployeeService, useValue: employeeServiceSpy },
        { provide: DepartmentService, useValue: departmentServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
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

    fixture = TestBed.createComponent(EmployeeListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have displayed columns defined', () => {
    expect(component.displayedColumns.length).toBeGreaterThan(0);
  });

  it('should have default page size of 10', () => {
    expect(component.pageSize).toBe(10);
  });

  it('should load employees on init', () => {
    component.ngOnInit();
    expect(employeeServiceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should load departments on init', () => {
    component.ngOnInit();
    expect(departmentServiceSpy.getAll).toHaveBeenCalled();
  });

  it('should handle employee load error', () => {
    employeeServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadEmployees();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load employees');
  });

  it('should handle department load error', () => {
    departmentServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadDepartments();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load departments');
  });

  it('should use search API when filters are active', () => {
    employeeServiceSpy.search.and.returnValue(of(mockPage));
    component.searchName = 'John';
    component.loadEmployees();
    expect(employeeServiceSpy.search).toHaveBeenCalled();
  });

  it('should handle search error', () => {
    employeeServiceSpy.search.and.returnValue(throwError(() => new Error('fail')));
    component.searchName = 'John';
    component.loadEmployees();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to search employees');
  });

  it('should reset page index on search', () => {
    component.pageIndex = 5;
    employeeServiceSpy.getAll.and.returnValue(of(mockPage));
    component.onSearch();
    expect(component.pageIndex).toBe(0);
  });

  it('should update pagination on page change', () => {
    component.onPageChange({ pageIndex: 2, pageSize: 25, length: 100 });
    expect(component.pageIndex).toBe(2);
    expect(component.pageSize).toBe(25);
  });

  it('should clear all filters', () => {
    component.searchName = 'test';
    component.selectedDepartmentId = 1;
    component.selectedStatus = 'ACTIVE';
    component.clearFilters();
    expect(component.searchName).toBe('');
    expect(component.selectedDepartmentId).toBeNull();
    expect(component.selectedStatus).toBe('');
  });

  it('should navigate to employee detail on view', () => {
    component.viewEmployee(mockPage.content[0]);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/employees', 1]);
  });

  it('should navigate to edit page', () => {
    component.editEmployee(mockPage.content[0]);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/employees', 1, 'edit']);
  });

  it('should delete employee after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    employeeServiceSpy.delete.and.returnValue(of(void 0));
    component.deleteEmployee(mockPage.content[0]);
    expect(employeeServiceSpy.delete).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalled();
  });

  it('should not delete employee if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteEmployee(mockPage.content[0]);
    expect(employeeServiceSpy.delete).not.toHaveBeenCalled();
  });

  it('should handle delete error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    employeeServiceSpy.delete.and.returnValue(throwError(() => new Error('fail')));
    component.deleteEmployee(mockPage.content[0]);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to delete employee');
  });

  it('should return correct status color', () => {
    expect(component.getStatusColor('ACTIVE')).toBe('primary');
    expect(component.getStatusColor('INACTIVE')).toBe('warn');
    expect(component.getStatusColor('ON_LEAVE')).toBe('accent');
    expect(component.getStatusColor('TERMINATED')).toBe('warn');
    expect(component.getStatusColor('UNKNOWN')).toBe('');
  });
});
