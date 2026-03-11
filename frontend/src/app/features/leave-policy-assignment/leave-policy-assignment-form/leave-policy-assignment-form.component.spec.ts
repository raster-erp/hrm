import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LeavePolicyAssignmentFormComponent } from './leave-policy-assignment-form.component';
import { LeavePolicyAssignmentService } from '../../../services/leave-policy-assignment.service';
import { LeavePolicyService } from '../../../services/leave-policy.service';
import { DepartmentService } from '../../../services/department.service';
import { DesignationService } from '../../../services/designation.service';
import { EmployeeService } from '../../../services/employee.service';
import { NotificationService } from '../../../services/notification.service';
import { LeavePolicyAssignmentResponse } from '../../../models/leave-policy-assignment.model';

describe('LeavePolicyAssignmentFormComponent', () => {
  let component: LeavePolicyAssignmentFormComponent;
  let fixture: ComponentFixture<LeavePolicyAssignmentFormComponent>;
  let assignmentServiceSpy: jasmine.SpyObj<LeavePolicyAssignmentService>;
  let leavePolicyServiceSpy: jasmine.SpyObj<LeavePolicyService>;
  let departmentServiceSpy: jasmine.SpyObj<DepartmentService>;
  let designationServiceSpy: jasmine.SpyObj<DesignationService>;
  let employeeServiceSpy: jasmine.SpyObj<EmployeeService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockAssignment: LeavePolicyAssignmentResponse = {
    id: 1, leavePolicyId: 1, leavePolicyName: 'Standard CL Policy',
    assignmentType: 'DEPARTMENT', departmentId: 1, departmentName: 'Engineering',
    designationId: null, designationTitle: null, employeeId: null, employeeName: null,
    effectiveFrom: '2026-01-01', effectiveTo: null,
    active: true,
    createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
  };

  const mockEmployeePage = {
    content: [
      { id: 1, firstName: 'John', lastName: 'Doe', employeeCode: 'E001', email: 'john@test.com',
        phone: '1234567890', dateOfBirth: '1990-01-01', gender: 'MALE', address: '', city: '',
        state: '', country: '', zipCode: '', emergencyContactName: '', emergencyContactPhone: '',
        bankName: '', bankAccountNumber: '', bankIfscCode: '', departmentId: 1,
        departmentName: 'Engineering', designationId: 1, designationName: 'Developer',
        joiningDate: '2020-01-01', employmentStatus: 'ACTIVE', photoUrl: '',
        createdAt: '2020-01-01', updatedAt: '2020-01-01' }
    ],
    totalElements: 1, totalPages: 1, size: 1000, number: 0,
    first: true, last: true, empty: false
  };

  function setup(routeId: string | null = null) {
    assignmentServiceSpy = jasmine.createSpyObj('LeavePolicyAssignmentService', ['getById', 'create', 'update']);
    leavePolicyServiceSpy = jasmine.createSpyObj('LeavePolicyService', ['getActive']);
    departmentServiceSpy = jasmine.createSpyObj('DepartmentService', ['getAll']);
    designationServiceSpy = jasmine.createSpyObj('DesignationService', ['getAll']);
    employeeServiceSpy = jasmine.createSpyObj('EmployeeService', ['getAll']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    leavePolicyServiceSpy.getActive.and.returnValue(of([]));
    departmentServiceSpy.getAll.and.returnValue(of([{ id: 1, name: 'Engineering', code: 'ENG', active: true, createdAt: '', updatedAt: '' }]));
    designationServiceSpy.getAll.and.returnValue(of([{ id: 1, title: 'Developer', code: 'DEV', level: 1, grade: 'A', departmentId: 1, departmentName: 'Engineering', createdAt: '', updatedAt: '' }]));
    employeeServiceSpy.getAll.and.returnValue(of(mockEmployeePage));

    TestBed.configureTestingModule({
      imports: [
        LeavePolicyAssignmentFormComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: LeavePolicyAssignmentService, useValue: assignmentServiceSpy },
        { provide: LeavePolicyService, useValue: leavePolicyServiceSpy },
        { provide: DepartmentService, useValue: departmentServiceSpy },
        { provide: DesignationService, useValue: designationServiceSpy },
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

    fixture = TestBed.createComponent(LeavePolicyAssignmentFormComponent);
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

    it('should initialize form group', () => {
      expect(component.assignmentForm).toBeDefined();
    });

    it('should require leave policy', () => {
      expect(component.assignmentForm.get('leavePolicyId')?.hasError('required')).toBeTrue();
    });

    it('should require assignment type', () => {
      expect(component.assignmentForm.get('assignmentType')?.hasError('required')).toBeTrue();
    });

    it('should require effective from', () => {
      expect(component.assignmentForm.get('effectiveFrom')?.hasError('required')).toBeTrue();
    });

    it('should load leave policies on init', () => {
      expect(leavePolicyServiceSpy.getActive).toHaveBeenCalled();
    });

    it('should load departments on init', () => {
      expect(departmentServiceSpy.getAll).toHaveBeenCalled();
      expect(component.departments.length).toBe(1);
    });

    it('should load designations on init', () => {
      expect(designationServiceSpy.getAll).toHaveBeenCalled();
      expect(component.designations.length).toBe(1);
    });

    it('should load employees on init', () => {
      expect(employeeServiceSpy.getAll).toHaveBeenCalled();
      expect(component.employees.length).toBe(1);
    });

    it('should show error if submitting invalid form', () => {
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Please fill in all required fields correctly');
    });

    it('should create assignment on valid submit', () => {
      fillValidForm();
      assignmentServiceSpy.create.and.returnValue(of(mockAssignment));
      component.onSubmit();
      expect(assignmentServiceSpy.create).toHaveBeenCalled();
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Policy assignment created successfully');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/leave-policy-assignments']);
    });

    it('should handle create error', () => {
      fillValidForm();
      assignmentServiceSpy.create.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to create policy assignment');
    });

    it('should navigate back on cancel', () => {
      component.onCancel();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/leave-policy-assignments']);
    });

    it('should handle department load error', () => {
      departmentServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
      component.loadDepartments();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load departments');
    });

    it('should handle designation load error', () => {
      designationServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
      component.loadDesignations();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load designations');
    });

    it('should handle employee load error', () => {
      employeeServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
      component.loadEmployees();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load employees');
    });
  });

  describe('edit mode', () => {
    beforeEach(() => {
      assignmentServiceSpy = jasmine.createSpyObj('LeavePolicyAssignmentService', ['getById', 'create', 'update']);
      leavePolicyServiceSpy = jasmine.createSpyObj('LeavePolicyService', ['getActive']);
      departmentServiceSpy = jasmine.createSpyObj('DepartmentService', ['getAll']);
      designationServiceSpy = jasmine.createSpyObj('DesignationService', ['getAll']);
      employeeServiceSpy = jasmine.createSpyObj('EmployeeService', ['getAll']);
      notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
      routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      assignmentServiceSpy.getById.and.returnValue(of(mockAssignment));
      leavePolicyServiceSpy.getActive.and.returnValue(of([]));
      departmentServiceSpy.getAll.and.returnValue(of([]));
      designationServiceSpy.getAll.and.returnValue(of([]));
      employeeServiceSpy.getAll.and.returnValue(of(mockEmployeePage));

      TestBed.configureTestingModule({
        imports: [LeavePolicyAssignmentFormComponent, HttpClientTestingModule, NoopAnimationsModule],
        providers: [
          { provide: LeavePolicyAssignmentService, useValue: assignmentServiceSpy },
          { provide: LeavePolicyService, useValue: leavePolicyServiceSpy },
          { provide: DepartmentService, useValue: departmentServiceSpy },
          { provide: DesignationService, useValue: designationServiceSpy },
          { provide: EmployeeService, useValue: employeeServiceSpy },
          { provide: NotificationService, useValue: notificationServiceSpy },
          { provide: Router, useValue: routerSpy },
          { provide: ActivatedRoute, useValue: { params: of({}), snapshot: { paramMap: { get: () => '1' } } } }
        ]
      });

      fixture = TestBed.createComponent(LeavePolicyAssignmentFormComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should be in edit mode', () => {
      expect(component.isEditMode).toBeTrue();
      expect(component.assignmentId).toBe(1);
    });

    it('should load assignment data', () => {
      expect(assignmentServiceSpy.getById).toHaveBeenCalledWith(1);
      expect(component.assignmentForm.get('assignmentType')?.value).toBe('DEPARTMENT');
    });

    it('should update assignment on submit', () => {
      assignmentServiceSpy.update.and.returnValue(of(mockAssignment));
      component.onSubmit();
      expect(assignmentServiceSpy.update).toHaveBeenCalledWith(1, jasmine.any(Object));
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Policy assignment updated successfully');
    });

    it('should handle update error', () => {
      assignmentServiceSpy.update.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to update policy assignment');
    });

    it('should handle load assignment error', () => {
      assignmentServiceSpy.getById.and.returnValue(throwError(() => new Error('fail')));
      component.loadAssignment(999);
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load policy assignment');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/leave-policy-assignments']);
    });
  });

  function fillValidForm() {
    component.assignmentForm.patchValue({
      leavePolicyId: 1,
      assignmentType: 'DEPARTMENT',
      departmentId: 1,
      designationId: null,
      employeeId: null,
      effectiveFrom: '2026-01-01',
      effectiveTo: null
    });
  }
});
