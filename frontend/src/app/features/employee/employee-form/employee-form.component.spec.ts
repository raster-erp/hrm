import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { EmployeeFormComponent } from './employee-form.component';
import { EmployeeService } from '../../../services/employee.service';
import { DepartmentService } from '../../../services/department.service';
import { DesignationService } from '../../../services/designation.service';
import { NotificationService } from '../../../services/notification.service';
import { EmployeeResponse } from '../../../models/employee.model';

describe('EmployeeFormComponent', () => {
  let component: EmployeeFormComponent;
  let fixture: ComponentFixture<EmployeeFormComponent>;
  let employeeServiceSpy: jasmine.SpyObj<EmployeeService>;
  let departmentServiceSpy: jasmine.SpyObj<DepartmentService>;
  let designationServiceSpy: jasmine.SpyObj<DesignationService>;
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

  function setup(routeId: string | null = null) {
    employeeServiceSpy = jasmine.createSpyObj('EmployeeService', ['getById', 'create', 'update']);
    departmentServiceSpy = jasmine.createSpyObj('DepartmentService', ['getAll']);
    designationServiceSpy = jasmine.createSpyObj('DesignationService', ['getByDepartment']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    departmentServiceSpy.getAll.and.returnValue(of([]));
    designationServiceSpy.getByDepartment.and.returnValue(of([]));

    TestBed.configureTestingModule({
      imports: [
        EmployeeFormComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: EmployeeService, useValue: employeeServiceSpy },
        { provide: DepartmentService, useValue: departmentServiceSpy },
        { provide: DesignationService, useValue: designationServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: Router, useValue: routerSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({}),
            snapshot: { paramMap: { get: () => routeId } }
          }
        }
      ]
    });

    fixture = TestBed.createComponent(EmployeeFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  describe('create mode', () => {
    beforeEach(() => setup(null));

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should be in create mode by default', () => {
      expect(component.isEditMode).toBeFalse();
    });

    it('should initialize all form groups', () => {
      expect(component.personalInfoForm).toBeDefined();
      expect(component.contactInfoForm).toBeDefined();
      expect(component.emergencyContactForm).toBeDefined();
      expect(component.bankDetailsForm).toBeDefined();
      expect(component.employmentInfoForm).toBeDefined();
    });

    it('should require personal info fields', () => {
      expect(component.personalInfoForm.get('firstName')?.hasError('required')).toBeTrue();
      expect(component.personalInfoForm.get('email')?.hasError('required')).toBeTrue();
    });

    it('should validate email format', () => {
      component.personalInfoForm.get('email')?.setValue('invalid-email');
      expect(component.personalInfoForm.get('email')?.hasError('email')).toBeTrue();
    });

    it('should validate phone format', () => {
      component.personalInfoForm.get('phone')?.setValue('abc');
      expect(component.personalInfoForm.get('phone')?.hasError('pattern')).toBeTrue();
    });

    it('should show error if submitting invalid form', () => {
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Please fill in all required fields correctly');
    });

    it('should create employee on valid submit', () => {
      fillValidForm();
      employeeServiceSpy.create.and.returnValue(of(mockEmployee));
      component.onSubmit();
      expect(employeeServiceSpy.create).toHaveBeenCalled();
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Employee created successfully');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/employees']);
    });

    it('should handle create error', () => {
      fillValidForm();
      employeeServiceSpy.create.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to create employee');
    });

    it('should navigate back on cancel', () => {
      component.onCancel();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/employees']);
    });

    it('should load designations on department change', () => {
      designationServiceSpy.getByDepartment.and.returnValue(of([{ id: 1, title: 'Dev', code: 'DEV', level: 1, grade: 'A', departmentId: 1, departmentName: 'Eng', createdAt: '', updatedAt: '' }]));
      component.onDepartmentChange(1);
      expect(designationServiceSpy.getByDepartment).toHaveBeenCalledWith(1);
      expect(component.filteredDesignations.length).toBe(1);
    });

    it('should clear designations when department change has no id', () => {
      component.filteredDesignations = [{ id: 1, title: 'Dev', code: 'DEV', level: 1, grade: 'A', departmentId: 1, departmentName: 'Eng', createdAt: '', updatedAt: '' }];
      component.onDepartmentChange(0);
      expect(component.filteredDesignations.length).toBe(0);
    });
  });

  describe('edit mode', () => {
    beforeEach(() => {
      employeeServiceSpy = jasmine.createSpyObj('EmployeeService', ['getById', 'create', 'update']);
      departmentServiceSpy = jasmine.createSpyObj('DepartmentService', ['getAll']);
      designationServiceSpy = jasmine.createSpyObj('DesignationService', ['getByDepartment']);
      notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
      routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      departmentServiceSpy.getAll.and.returnValue(of([]));
      designationServiceSpy.getByDepartment.and.returnValue(of([]));
      employeeServiceSpy.getById.and.returnValue(of(mockEmployee));

      TestBed.configureTestingModule({
        imports: [EmployeeFormComponent, HttpClientTestingModule, NoopAnimationsModule],
        providers: [
          { provide: EmployeeService, useValue: employeeServiceSpy },
          { provide: DepartmentService, useValue: departmentServiceSpy },
          { provide: DesignationService, useValue: designationServiceSpy },
          { provide: NotificationService, useValue: notificationServiceSpy },
          { provide: Router, useValue: routerSpy },
          { provide: ActivatedRoute, useValue: { params: of({}), snapshot: { paramMap: { get: () => '1' } } } }
        ]
      });

      fixture = TestBed.createComponent(EmployeeFormComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should be in edit mode', () => {
      expect(component.isEditMode).toBeTrue();
      expect(component.employeeId).toBe(1);
    });

    it('should load employee data', () => {
      expect(employeeServiceSpy.getById).toHaveBeenCalledWith(1);
      expect(component.personalInfoForm.get('firstName')?.value).toBe('John');
    });

    it('should update employee on submit', () => {
      employeeServiceSpy.update.and.returnValue(of(mockEmployee));
      component.onSubmit();
      expect(employeeServiceSpy.update).toHaveBeenCalledWith(1, jasmine.any(Object));
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Employee updated successfully');
    });

    it('should handle update error', () => {
      employeeServiceSpy.update.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to update employee');
    });

    it('should handle load employee error', () => {
      employeeServiceSpy.getById.and.returnValue(throwError(() => new Error('fail')));
      component.loadEmployee(999);
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load employee');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/employees']);
    });
  });

  function fillValidForm() {
    component.personalInfoForm.patchValue({
      firstName: 'John', lastName: 'Doe', email: 'john@example.com',
      phone: '1234567890', dateOfBirth: '1990-01-01', gender: 'MALE'
    });
    component.contactInfoForm.patchValue({
      address: '123 St', city: 'City', state: 'State', country: 'Country', zipCode: '12345'
    });
    component.emergencyContactForm.patchValue({
      emergencyContactName: 'Jane', emergencyContactPhone: '0987654321'
    });
    component.bankDetailsForm.patchValue({
      bankName: 'Bank', bankAccountNumber: '111', bankIfscCode: 'IFSC001'
    });
    component.employmentInfoForm.patchValue({
      employeeCode: 'EMP001', departmentId: 1, designationId: 1,
      joiningDate: '2023-01-01', employmentStatus: 'ACTIVE'
    });
  }
});
