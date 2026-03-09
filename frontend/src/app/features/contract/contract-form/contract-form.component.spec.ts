import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ContractFormComponent } from './contract-form.component';
import { ContractService } from '../../../services/contract.service';
import { EmployeeService } from '../../../services/employee.service';
import { NotificationService } from '../../../services/notification.service';
import { ContractResponse } from '../../../models/contract.model';
import { Page } from '../../../models/page.model';
import { EmployeeResponse } from '../../../models/employee.model';

describe('ContractFormComponent', () => {
  let component: ContractFormComponent;
  let fixture: ComponentFixture<ContractFormComponent>;
  let contractServiceSpy: jasmine.SpyObj<ContractService>;
  let employeeServiceSpy: jasmine.SpyObj<EmployeeService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockContract: ContractResponse = {
    id: 1, employeeId: 1, employeeCode: 'EMP001', employeeName: 'John Doe',
    contractType: 'PERMANENT', startDate: '2023-01-01', endDate: '2024-01-01',
    terms: 'Standard terms', status: 'ACTIVE', createdAt: '', updatedAt: ''
  };

  const mockEmployeePage: Page<EmployeeResponse> = {
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
    totalElements: 1, totalPages: 1, size: 1000, number: 0,
    first: true, last: true, empty: false
  };

  function setup(routeId: string | null = null) {
    contractServiceSpy = jasmine.createSpyObj('ContractService', ['getById', 'create', 'update']);
    employeeServiceSpy = jasmine.createSpyObj('EmployeeService', ['getAll']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    employeeServiceSpy.getAll.and.returnValue(of(mockEmployeePage));

    TestBed.configureTestingModule({
      imports: [
        ContractFormComponent,
        NoopAnimationsModule
      ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ContractService, useValue: contractServiceSpy },
        { provide: EmployeeService, useValue: employeeServiceSpy },
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

    fixture = TestBed.createComponent(ContractFormComponent);
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

    it('should initialize contract form', () => {
      expect(component.contractForm).toBeDefined();
      expect(component.contractForm.get('employeeId')).toBeTruthy();
      expect(component.contractForm.get('contractType')).toBeTruthy();
      expect(component.contractForm.get('startDate')).toBeTruthy();
      expect(component.contractForm.get('endDate')).toBeTruthy();
      expect(component.contractForm.get('terms')).toBeTruthy();
      expect(component.contractForm.get('status')).toBeTruthy();
    });

    it('should require employee', () => {
      expect(component.contractForm.get('employeeId')?.hasError('required')).toBeTrue();
    });

    it('should require contract type', () => {
      expect(component.contractForm.get('contractType')?.hasError('required')).toBeTrue();
    });

    it('should require start date', () => {
      expect(component.contractForm.get('startDate')?.hasError('required')).toBeTrue();
    });

    it('should require end date', () => {
      expect(component.contractForm.get('endDate')?.hasError('required')).toBeTrue();
    });

    it('should default status to ACTIVE', () => {
      expect(component.contractForm.get('status')?.value).toBe('ACTIVE');
    });

    it('should load employees on init', () => {
      expect(employeeServiceSpy.getAll).toHaveBeenCalledWith(0, 1000);
      expect(component.employees.length).toBe(1);
    });

    it('should handle employee load error', () => {
      employeeServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
      component.loadEmployees();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load employees');
    });

    it('should show error if submitting invalid form', () => {
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Please fill in all required fields correctly');
    });

    it('should create contract on valid submit', () => {
      fillValidForm();
      contractServiceSpy.create.and.returnValue(of(mockContract));
      component.onSubmit();
      expect(contractServiceSpy.create).toHaveBeenCalled();
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Contract created successfully');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/contracts']);
    });

    it('should handle create error', () => {
      fillValidForm();
      contractServiceSpy.create.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to create contract');
    });

    it('should navigate back on cancel', () => {
      component.onCancel();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/contracts']);
    });

    it('should format contract type', () => {
      expect(component.formatType('FIXED_TERM')).toBe('FIXED TERM');
    });
  });

  describe('edit mode', () => {
    beforeEach(() => {
      contractServiceSpy = jasmine.createSpyObj('ContractService', ['getById', 'create', 'update']);
      employeeServiceSpy = jasmine.createSpyObj('EmployeeService', ['getAll']);
      notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
      routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      employeeServiceSpy.getAll.and.returnValue(of(mockEmployeePage));
      contractServiceSpy.getById.and.returnValue(of(mockContract));

      TestBed.configureTestingModule({
        imports: [ContractFormComponent, NoopAnimationsModule],
        providers: [
          provideHttpClient(),
          provideHttpClientTesting(),
          { provide: ContractService, useValue: contractServiceSpy },
          { provide: EmployeeService, useValue: employeeServiceSpy },
          { provide: NotificationService, useValue: notificationServiceSpy },
          { provide: Router, useValue: routerSpy },
          { provide: ActivatedRoute, useValue: { params: of({}), snapshot: { paramMap: { get: () => '1' } } } }
        ]
      });

      fixture = TestBed.createComponent(ContractFormComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should be in edit mode', () => {
      expect(component.isEditMode).toBeTrue();
      expect(component.contractId).toBe(1);
    });

    it('should load contract data', () => {
      expect(contractServiceSpy.getById).toHaveBeenCalledWith(1);
      expect(component.contractForm.get('contractType')?.value).toBe('PERMANENT');
    });

    it('should update contract on submit', () => {
      contractServiceSpy.update.and.returnValue(of(mockContract));
      component.onSubmit();
      expect(contractServiceSpy.update).toHaveBeenCalledWith(1, jasmine.any(Object));
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Contract updated successfully');
    });

    it('should handle update error', () => {
      contractServiceSpy.update.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to update contract');
    });

    it('should handle load contract error', () => {
      contractServiceSpy.getById.and.returnValue(throwError(() => new Error('fail')));
      component.loadContract(999);
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load contract');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/contracts']);
    });
  });

  function fillValidForm() {
    component.contractForm.patchValue({
      employeeId: 1,
      contractType: 'PERMANENT',
      startDate: '2023-01-01',
      endDate: '2024-01-01',
      terms: 'Standard terms',
      status: 'ACTIVE'
    });
  }
});
