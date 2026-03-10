import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { BulkAssignWizardComponent } from './bulk-assign-wizard.component';
import { ShiftRosterService } from '../../../services/shift-roster.service';
import { ShiftService } from '../../../services/shift.service';
import { RotationPatternService } from '../../../services/rotation-pattern.service';
import { EmployeeService } from '../../../services/employee.service';
import { DepartmentService } from '../../../services/department.service';
import { NotificationService } from '../../../services/notification.service';
import { ShiftRosterResponse } from '../../../models/shift-roster.model';
import { EmployeeResponse } from '../../../models/employee.model';
import { DepartmentResponse } from '../../../models/department.model';
import { ShiftResponse } from '../../../models/shift.model';
import { RotationPatternResponse } from '../../../models/rotation-pattern.model';
import { Page } from '../../../models/page.model';

describe('BulkAssignWizardComponent', () => {
  let component: BulkAssignWizardComponent;
  let fixture: ComponentFixture<BulkAssignWizardComponent>;
  let shiftRosterServiceSpy: jasmine.SpyObj<ShiftRosterService>;
  let shiftServiceSpy: jasmine.SpyObj<ShiftService>;
  let rotationPatternServiceSpy: jasmine.SpyObj<RotationPatternService>;
  let employeeServiceSpy: jasmine.SpyObj<EmployeeService>;
  let departmentServiceSpy: jasmine.SpyObj<DepartmentService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockEmployees: EmployeeResponse[] = [
    {
      id: 1, employeeCode: 'EMP-001', firstName: 'John', lastName: 'Doe',
      departmentId: 10, departmentName: 'Engineering',
      email: '', phone: '', dateOfBirth: '', gender: '', address: '', city: '', state: '',
      country: '', zipCode: '', emergencyContactName: '', emergencyContactPhone: '',
      bankName: '', bankAccountNumber: '', bankIfscCode: '',
      designationId: 1, designationName: 'Developer',
      joiningDate: '2024-01-01', employmentStatus: 'ACTIVE',
      photoUrl: '', createdAt: '', updatedAt: ''
    },
    {
      id: 2, employeeCode: 'EMP-002', firstName: 'Jane', lastName: 'Smith',
      departmentId: 20, departmentName: 'HR',
      email: '', phone: '', dateOfBirth: '', gender: '', address: '', city: '', state: '',
      country: '', zipCode: '', emergencyContactName: '', emergencyContactPhone: '',
      bankName: '', bankAccountNumber: '', bankIfscCode: '',
      designationId: 2, designationName: 'Manager',
      joiningDate: '2024-02-01', employmentStatus: 'ACTIVE',
      photoUrl: '', createdAt: '', updatedAt: ''
    }
  ];

  const mockDepartments: DepartmentResponse[] = [
    { id: 10, name: 'Engineering', code: 'ENG', active: true, createdAt: '', updatedAt: '' },
    { id: 20, name: 'HR', code: 'HR', active: true, createdAt: '', updatedAt: '' }
  ];

  const mockShifts: ShiftResponse[] = [
    {
      id: 1, name: 'Morning Shift', type: 'FIXED', startTime: '09:00', endTime: '17:00',
      breakDurationMinutes: 60, gracePeriodMinutes: 15, description: '',
      active: true, createdAt: '', updatedAt: ''
    }
  ];

  const mockPatternPage: Page<RotationPatternResponse> = {
    content: [
      { id: 1, name: 'Weekly Rotation', description: '', rotationDays: 7, shiftSequence: '', createdAt: '', updatedAt: '' }
    ],
    totalElements: 1, totalPages: 1, size: 100, number: 0,
    first: true, last: true, empty: false
  };

  const mockEmployeePage: Page<EmployeeResponse> = {
    content: mockEmployees,
    totalElements: 2, totalPages: 1, size: 200, number: 0,
    first: true, last: true, empty: false
  };

  const mockRosterResponses: ShiftRosterResponse[] = [
    {
      id: 1, employeeId: 1, employeeName: 'John Doe', employeeCode: 'EMP-001',
      shiftId: 1, shiftName: 'Morning Shift',
      effectiveDate: '2026-07-01', endDate: '2026-12-31',
      rotationPatternId: null, rotationPatternName: null,
      createdAt: '2026-01-01T00:00:00', updatedAt: '2026-01-01T00:00:00'
    }
  ];

  function setup() {
    shiftRosterServiceSpy = jasmine.createSpyObj('ShiftRosterService', ['bulkCreate']);
    shiftServiceSpy = jasmine.createSpyObj('ShiftService', ['getActive']);
    rotationPatternServiceSpy = jasmine.createSpyObj('RotationPatternService', ['getAll']);
    employeeServiceSpy = jasmine.createSpyObj('EmployeeService', ['getAll']);
    departmentServiceSpy = jasmine.createSpyObj('DepartmentService', ['getAll']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    shiftServiceSpy.getActive.and.returnValue(of(mockShifts));
    rotationPatternServiceSpy.getAll.and.returnValue(of(mockPatternPage));
    employeeServiceSpy.getAll.and.returnValue(of(mockEmployeePage));
    departmentServiceSpy.getAll.and.returnValue(of(mockDepartments));

    TestBed.configureTestingModule({
      imports: [
        BulkAssignWizardComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: ShiftRosterService, useValue: shiftRosterServiceSpy },
        { provide: ShiftService, useValue: shiftServiceSpy },
        { provide: RotationPatternService, useValue: rotationPatternServiceSpy },
        { provide: EmployeeService, useValue: employeeServiceSpy },
        { provide: DepartmentService, useValue: departmentServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });

    fixture = TestBed.createComponent(BulkAssignWizardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  beforeEach(() => setup());

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load data on init', () => {
    expect(departmentServiceSpy.getAll).toHaveBeenCalled();
    expect(employeeServiceSpy.getAll).toHaveBeenCalledWith(0, 200);
    expect(shiftServiceSpy.getActive).toHaveBeenCalled();
    expect(rotationPatternServiceSpy.getAll).toHaveBeenCalledWith(0, 100);
  });

  it('should populate employees from service', () => {
    expect(component.employees.length).toBe(2);
  });

  it('should populate departments from service', () => {
    expect(component.departments.length).toBe(2);
  });

  describe('employee selection', () => {
    it('should toggle employee selection', () => {
      component.toggleEmployee(1);
      expect(component.isEmployeeSelected(1)).toBeTrue();
      component.toggleEmployee(1);
      expect(component.isEmployeeSelected(1)).toBeFalse();
    });

    it('should select all filtered employees', () => {
      component.selectAll();
      expect(component.selectedEmployeeIds.size).toBe(2);
    });

    it('should deselect all filtered employees', () => {
      component.selectAll();
      component.deselectAll();
      expect(component.selectedEmployeeIds.size).toBe(0);
    });

    it('should filter employees by department', () => {
      component.onDepartmentFilterChange(10);
      expect(component.filteredEmployees.length).toBe(1);
      expect(component.filteredEmployees[0].departmentId).toBe(10);
    });

    it('should show all employees when department filter is null', () => {
      component.onDepartmentFilterChange(null);
      expect(component.filteredEmployees.length).toBe(2);
    });

    it('should select all only within filtered department', () => {
      component.onDepartmentFilterChange(10);
      component.selectAll();
      expect(component.selectedEmployeeIds.size).toBe(1);
      expect(component.isEmployeeSelected(1)).toBeTrue();
      expect(component.isEmployeeSelected(2)).toBeFalse();
    });

    it('should deselect all only within filtered department', () => {
      component.selectAll();
      component.onDepartmentFilterChange(10);
      component.deselectAll();
      expect(component.isEmployeeSelected(1)).toBeFalse();
      expect(component.isEmployeeSelected(2)).toBeTrue();
    });
  });

  describe('step validation', () => {
    it('should disable next when no employees selected', () => {
      expect(component.isEmployeeStepValid).toBeFalse();
    });

    it('should enable next when employees are selected', () => {
      component.toggleEmployee(1);
      expect(component.isEmployeeStepValid).toBeTrue();
    });

    it('should require shift in form', () => {
      expect(component.shiftForm.get('shiftId')?.hasError('required')).toBeTrue();
    });

    it('should require effective date in form', () => {
      expect(component.shiftForm.get('effectiveDate')?.hasError('required')).toBeTrue();
    });

    it('should be valid when required fields are filled', () => {
      component.shiftForm.patchValue({ shiftId: 1, effectiveDate: '2026-07-01' });
      expect(component.shiftForm.valid).toBeTrue();
    });
  });

  describe('review step', () => {
    it('should return selected shift name', () => {
      component.shiftForm.patchValue({ shiftId: 1 });
      expect(component.selectedShiftName).toBe('Morning Shift');
    });

    it('should return empty string for unknown shift', () => {
      component.shiftForm.patchValue({ shiftId: 999 });
      expect(component.selectedShiftName).toBe('');
    });

    it('should return selected rotation pattern name', () => {
      component.shiftForm.patchValue({ rotationPatternId: 1 });
      expect(component.selectedRotationPatternName).toBe('Weekly Rotation');
    });

    it('should return empty string when no rotation pattern selected', () => {
      expect(component.selectedRotationPatternName).toBe('');
    });

    it('should return selected employees list', () => {
      component.toggleEmployee(1);
      const selected = component.selectedEmployees;
      expect(selected.length).toBe(1);
      expect(selected[0].id).toBe(1);
    });
  });

  describe('submit', () => {
    beforeEach(() => {
      component.toggleEmployee(1);
      component.toggleEmployee(2);
      component.shiftForm.patchValue({
        shiftId: 1,
        effectiveDate: '2026-07-01',
        endDate: '2026-12-31',
        rotationPatternId: 1
      });
    });

    it('should not submit if form is invalid', () => {
      component.shiftForm.patchValue({ shiftId: null });
      component.onSubmit();
      expect(shiftRosterServiceSpy.bulkCreate).not.toHaveBeenCalled();
    });

    it('should not submit if no employees selected', () => {
      component.deselectAll();
      component.onDepartmentFilterChange(null);
      component.deselectAll();
      component.onSubmit();
      expect(shiftRosterServiceSpy.bulkCreate).not.toHaveBeenCalled();
    });

    it('should call bulkCreate with correct request', () => {
      shiftRosterServiceSpy.bulkCreate.and.returnValue(of(mockRosterResponses));
      component.onSubmit();
      expect(shiftRosterServiceSpy.bulkCreate).toHaveBeenCalledWith(jasmine.objectContaining({
        employeeIds: jasmine.arrayContaining([1, 2]),
        shiftId: 1,
        effectiveDate: '2026-07-01',
        endDate: '2026-12-31',
        rotationPatternId: 1
      }));
    });

    it('should show success notification on submit', () => {
      shiftRosterServiceSpy.bulkCreate.and.returnValue(of(mockRosterResponses));
      component.onSubmit();
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Successfully created 1 roster assignments');
    });

    it('should navigate to shift-rosters on success', () => {
      shiftRosterServiceSpy.bulkCreate.and.returnValue(of(mockRosterResponses));
      component.onSubmit();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/shift-rosters']);
    });

    it('should set submitting flag during submit', () => {
      shiftRosterServiceSpy.bulkCreate.and.returnValue(of(mockRosterResponses));
      component.onSubmit();
      expect(component.submitting).toBeFalse();
    });

    it('should handle submit error', () => {
      shiftRosterServiceSpy.bulkCreate.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to create bulk roster assignments');
      expect(component.submitting).toBeFalse();
    });

    it('should send null for optional empty fields', () => {
      component.shiftForm.patchValue({ endDate: '', rotationPatternId: null });
      shiftRosterServiceSpy.bulkCreate.and.returnValue(of(mockRosterResponses));
      component.onSubmit();
      expect(shiftRosterServiceSpy.bulkCreate).toHaveBeenCalledWith(jasmine.objectContaining({
        endDate: null,
        rotationPatternId: null
      }));
    });
  });

  it('should navigate on cancel', () => {
    component.onCancel();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/shift-rosters']);
  });

  it('should handle employee load error', () => {
    employeeServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.ngOnInit();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load employees');
  });

  it('should handle department load error', () => {
    departmentServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.ngOnInit();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load departments');
  });

  it('should handle shift load error', () => {
    shiftServiceSpy.getActive.and.returnValue(throwError(() => new Error('fail')));
    component.ngOnInit();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load shifts');
  });

  it('should handle rotation pattern load error', () => {
    rotationPatternServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.ngOnInit();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load rotation patterns');
  });
});
