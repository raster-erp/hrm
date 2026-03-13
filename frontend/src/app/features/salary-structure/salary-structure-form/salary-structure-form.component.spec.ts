import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { SalaryStructureFormComponent } from './salary-structure-form.component';
import { SalaryStructureService } from '../../../services/salary-structure.service';
import { SalaryComponentService } from '../../../services/salary-component.service';
import { NotificationService } from '../../../services/notification.service';
import { SalaryStructureResponse } from '../../../models/salary-structure.model';

describe('SalaryStructureFormComponent', () => {
  let component: SalaryStructureFormComponent;
  let fixture: ComponentFixture<SalaryStructureFormComponent>;
  let structureServiceSpy: jasmine.SpyObj<SalaryStructureService>;
  let componentServiceSpy: jasmine.SpyObj<SalaryComponentService>;
  let notificationSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockStructure: SalaryStructureResponse = {
    id: 1, code: 'STD', name: 'Standard', description: 'Standard structure',
    active: true, components: [
      {
        id: 1, salaryComponentId: 1, salaryComponentCode: 'BASIC',
        salaryComponentName: 'Basic Salary', salaryComponentType: 'EARNING',
        computationType: 'FIXED', percentageValue: null, fixedAmount: 10000,
        sortOrder: 0, createdAt: '2026-01-01T00:00:00'
      }
    ],
    createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
  };

  function setup(routeId: string | null = null) {
    structureServiceSpy = jasmine.createSpyObj('SalaryStructureService', ['getById', 'create', 'update', 'getActive']);
    componentServiceSpy = jasmine.createSpyObj('SalaryComponentService', ['getActive']);
    notificationSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    componentServiceSpy.getActive.and.returnValue(of([]));

    TestBed.configureTestingModule({
      imports: [
        SalaryStructureFormComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: SalaryStructureService, useValue: structureServiceSpy },
        { provide: SalaryComponentService, useValue: componentServiceSpy },
        { provide: NotificationService, useValue: notificationSpy },
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

    fixture = TestBed.createComponent(SalaryStructureFormComponent);
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
      expect(component.structureForm).toBeDefined();
    });

    it('should require code', () => {
      expect(component.structureForm.get('code')?.hasError('required')).toBeTrue();
    });

    it('should require name', () => {
      expect(component.structureForm.get('name')?.hasError('required')).toBeTrue();
    });

    it('should load available components', () => {
      expect(componentServiceSpy.getActive).toHaveBeenCalled();
    });

    it('should show error if submitting invalid form', () => {
      component.onSubmit();
      expect(notificationSpy.error).toHaveBeenCalledWith('Please fill in all required fields correctly');
    });

    it('should create structure on valid submit', () => {
      fillValidForm();
      structureServiceSpy.create.and.returnValue(of(mockStructure));
      component.onSubmit();
      expect(structureServiceSpy.create).toHaveBeenCalled();
      expect(notificationSpy.success).toHaveBeenCalledWith('Structure created successfully');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/salary-structures']);
    });

    it('should handle create error', () => {
      fillValidForm();
      structureServiceSpy.create.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationSpy.error).toHaveBeenCalledWith('Failed to create structure');
    });

    it('should add component to form array', () => {
      component.addComponent();
      expect(component.componentsArray.length).toBe(1);
    });

    it('should remove component from form array', () => {
      component.addComponent();
      component.addComponent();
      component.removeComponent(0);
      expect(component.componentsArray.length).toBe(1);
    });

    it('should navigate back on cancel', () => {
      component.onCancel();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/salary-structures']);
    });
  });

  describe('edit mode', () => {
    beforeEach(() => {
      structureServiceSpy = jasmine.createSpyObj('SalaryStructureService', ['getById', 'create', 'update', 'getActive']);
      componentServiceSpy = jasmine.createSpyObj('SalaryComponentService', ['getActive']);
      notificationSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
      routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      structureServiceSpy.getById.and.returnValue(of(mockStructure));
      componentServiceSpy.getActive.and.returnValue(of([]));

      TestBed.configureTestingModule({
        imports: [SalaryStructureFormComponent, HttpClientTestingModule, NoopAnimationsModule],
        providers: [
          { provide: SalaryStructureService, useValue: structureServiceSpy },
          { provide: SalaryComponentService, useValue: componentServiceSpy },
          { provide: NotificationService, useValue: notificationSpy },
          { provide: Router, useValue: routerSpy },
          { provide: ActivatedRoute, useValue: { params: of({}), snapshot: { paramMap: { get: () => '1' } } } }
        ]
      });

      fixture = TestBed.createComponent(SalaryStructureFormComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should be in edit mode', () => {
      expect(component.isEditMode).toBeTrue();
      expect(component.structureId).toBe(1);
    });

    it('should load structure data', () => {
      expect(structureServiceSpy.getById).toHaveBeenCalledWith(1);
      expect(component.structureForm.get('name')?.value).toBe('Standard');
    });

    it('should load components into form array', () => {
      expect(component.componentsArray.length).toBe(1);
    });

    it('should update structure on submit', () => {
      structureServiceSpy.update.and.returnValue(of(mockStructure));
      component.onSubmit();
      expect(structureServiceSpy.update).toHaveBeenCalledWith(1, jasmine.any(Object));
      expect(notificationSpy.success).toHaveBeenCalledWith('Structure updated successfully');
    });

    it('should handle update error', () => {
      structureServiceSpy.update.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationSpy.error).toHaveBeenCalledWith('Failed to update structure');
    });

    it('should handle load structure error', () => {
      structureServiceSpy.getById.and.returnValue(throwError(() => new Error('fail')));
      component.loadStructure(999);
      expect(notificationSpy.error).toHaveBeenCalledWith('Failed to load salary structure');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/salary-structures']);
    });
  });

  function fillValidForm() {
    component.structureForm.patchValue({
      code: 'STD',
      name: 'Standard',
      description: 'Standard structure'
    });
  }
});
