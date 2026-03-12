import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { EmployeeSalaryFormComponent } from './employee-salary-form.component';
import { EmployeeSalaryService } from '../../../services/employee-salary.service';
import { SalaryStructureService } from '../../../services/salary-structure.service';
import { NotificationService } from '../../../services/notification.service';
import { EmployeeSalaryDetailResponse } from '../../../models/employee-salary.model';
import { SalaryStructureResponse } from '../../../models/salary-structure.model';

describe('EmployeeSalaryFormComponent', () => {
  let component: EmployeeSalaryFormComponent;
  let fixture: ComponentFixture<EmployeeSalaryFormComponent>;
  let employeeSalaryServiceSpy: jasmine.SpyObj<EmployeeSalaryService>;
  let structureServiceSpy: jasmine.SpyObj<SalaryStructureService>;
  let notificationSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockStructures: SalaryStructureResponse[] = [
    {
      id: 1, code: 'STD', name: 'Standard', description: 'Standard structure',
      active: true, components: [
        {
          id: 1, salaryComponentId: 1, salaryComponentCode: 'BASIC',
          salaryComponentName: 'Basic Salary', salaryComponentType: 'EARNING',
          computationType: 'FIXED', percentageValue: null, fixedAmount: 10000,
          sortOrder: 0, createdAt: '2026-01-01T00:00:00'
        },
        {
          id: 2, salaryComponentId: 2, salaryComponentCode: 'HRA',
          salaryComponentName: 'HRA', salaryComponentType: 'EARNING',
          computationType: 'PERCENTAGE_OF_BASIC', percentageValue: 50, fixedAmount: null,
          sortOrder: 1, createdAt: '2026-01-01T00:00:00'
        }
      ],
      createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
    }
  ];

  const mockResponse: EmployeeSalaryDetailResponse = {
    id: 1, employeeId: 1, employeeName: 'John Doe', employeeCode: 'EMP001',
    salaryStructureId: 1, salaryStructureName: 'Standard',
    ctc: 1200000, basicSalary: 50000,
    effectiveDate: '2024-04-01', notes: 'Initial', active: true,
    createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
  };

  beforeEach(async () => {
    employeeSalaryServiceSpy = jasmine.createSpyObj('EmployeeSalaryService', ['create']);
    structureServiceSpy = jasmine.createSpyObj('SalaryStructureService', ['getActive']);
    notificationSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    structureServiceSpy.getActive.and.returnValue(of(mockStructures));

    await TestBed.configureTestingModule({
      imports: [
        EmployeeSalaryFormComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: EmployeeSalaryService, useValue: employeeSalaryServiceSpy },
        { provide: SalaryStructureService, useValue: structureServiceSpy },
        { provide: NotificationService, useValue: notificationSpy },
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

    fixture = TestBed.createComponent(EmployeeSalaryFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form group', () => {
    expect(component.salaryForm).toBeDefined();
  });

  it('should require employeeId', () => {
    expect(component.salaryForm.get('employeeId')?.hasError('required')).toBeTrue();
  });

  it('should require salaryStructureId', () => {
    expect(component.salaryForm.get('salaryStructureId')?.hasError('required')).toBeTrue();
  });

  it('should require ctc', () => {
    expect(component.salaryForm.get('ctc')?.hasError('required')).toBeTrue();
  });

  it('should require basicSalary', () => {
    expect(component.salaryForm.get('basicSalary')?.hasError('required')).toBeTrue();
  });

  it('should load available structures', () => {
    expect(structureServiceSpy.getActive).toHaveBeenCalled();
    expect(component.availableStructures.length).toBe(1);
  });

  it('should show error if submitting invalid form', () => {
    component.onSubmit();
    expect(notificationSpy.error).toHaveBeenCalledWith('Please fill in all required fields correctly');
  });

  it('should create salary assignment on valid submit', () => {
    fillValidForm();
    employeeSalaryServiceSpy.create.and.returnValue(of(mockResponse));
    component.onSubmit();
    expect(employeeSalaryServiceSpy.create).toHaveBeenCalled();
    expect(notificationSpy.success).toHaveBeenCalledWith('Salary assigned successfully');
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/employee-salary']);
  });

  it('should handle create error', () => {
    fillValidForm();
    employeeSalaryServiceSpy.create.and.returnValue(throwError(() => new Error('fail')));
    component.onSubmit();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to assign salary');
  });

  it('should navigate back on cancel', () => {
    component.onCancel();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/employee-salary']);
  });

  it('should calculate CTC breakdown on structure change', () => {
    component.salaryForm.patchValue({ salaryStructureId: 1, basicSalary: 50000 });
    component.onStructureChange();
    expect(component.ctcBreakdown.length).toBe(2);
  });

  it('should calculate breakdown with FIXED computation', () => {
    component.salaryForm.patchValue({ salaryStructureId: 1, basicSalary: 50000 });
    component.onStructureChange();
    const basicItem = component.ctcBreakdown.find(i => i.componentName === 'Basic Salary');
    expect(basicItem?.monthlyAmount).toBe(10000);
    expect(basicItem?.annualAmount).toBe(120000);
  });

  it('should calculate breakdown with PERCENTAGE_OF_BASIC computation', () => {
    component.salaryForm.patchValue({ salaryStructureId: 1, basicSalary: 50000 });
    component.onStructureChange();
    const hraItem = component.ctcBreakdown.find(i => i.componentName === 'HRA');
    expect(hraItem?.monthlyAmount).toBe(25000);
    expect(hraItem?.annualAmount).toBe(300000);
  });

  it('should calculate total monthly and annual', () => {
    component.salaryForm.patchValue({ salaryStructureId: 1, basicSalary: 50000 });
    component.onStructureChange();
    expect(component.getTotalMonthly()).toBe(35000);
    expect(component.getTotalAnnual()).toBe(420000);
  });

  it('should clear breakdown when structure is cleared', () => {
    component.salaryForm.patchValue({ salaryStructureId: null });
    component.onStructureChange();
    expect(component.ctcBreakdown.length).toBe(0);
  });

  it('should handle load structures error', () => {
    structureServiceSpy.getActive.and.returnValue(throwError(() => new Error('fail')));
    component.loadStructures();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to load salary structures');
  });

  function fillValidForm() {
    component.salaryForm.patchValue({
      employeeId: 1,
      salaryStructureId: 1,
      ctc: 1200000,
      basicSalary: 50000,
      effectiveDate: '2024-04-01',
      notes: 'Initial'
    });
  }
});
