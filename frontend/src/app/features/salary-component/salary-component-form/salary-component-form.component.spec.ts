import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { SalaryComponentFormComponent } from './salary-component-form.component';
import { SalaryComponentService } from '../../../services/salary-component.service';
import { NotificationService } from '../../../services/notification.service';
import { SalaryComponentResponse } from '../../../models/salary-component.model';

describe('SalaryComponentFormComponent', () => {
  let component: SalaryComponentFormComponent;
  let fixture: ComponentFixture<SalaryComponentFormComponent>;
  let serviceSpy: jasmine.SpyObj<SalaryComponentService>;
  let notificationSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockComponent: SalaryComponentResponse = {
    id: 1, code: 'BASIC', name: 'Basic Salary', type: 'EARNING',
    computationType: 'FIXED', percentageValue: null, taxable: true,
    mandatory: true, description: 'Basic salary', active: true,
    createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
  };

  function setup(routeId: string | null = null) {
    serviceSpy = jasmine.createSpyObj('SalaryComponentService', ['getById', 'create', 'update']);
    notificationSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [
        SalaryComponentFormComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: SalaryComponentService, useValue: serviceSpy },
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

    fixture = TestBed.createComponent(SalaryComponentFormComponent);
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
      expect(component.componentForm).toBeDefined();
    });

    it('should require code', () => {
      expect(component.componentForm.get('code')?.hasError('required')).toBeTrue();
    });

    it('should require name', () => {
      expect(component.componentForm.get('name')?.hasError('required')).toBeTrue();
    });

    it('should require type', () => {
      expect(component.componentForm.get('type')?.hasError('required')).toBeTrue();
    });

    it('should require computationType', () => {
      expect(component.componentForm.get('computationType')?.hasError('required')).toBeTrue();
    });

    it('should show error if submitting invalid form', () => {
      component.onSubmit();
      expect(notificationSpy.error).toHaveBeenCalledWith('Please fill in all required fields correctly');
    });

    it('should create component on valid submit', () => {
      fillValidForm();
      serviceSpy.create.and.returnValue(of(mockComponent));
      component.onSubmit();
      expect(serviceSpy.create).toHaveBeenCalled();
      expect(notificationSpy.success).toHaveBeenCalledWith('Component created successfully');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/salary-components']);
    });

    it('should handle create error', () => {
      fillValidForm();
      serviceSpy.create.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationSpy.error).toHaveBeenCalledWith('Failed to create component');
    });

    it('should navigate back on cancel', () => {
      component.onCancel();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/salary-components']);
    });
  });

  describe('edit mode', () => {
    beforeEach(() => {
      serviceSpy = jasmine.createSpyObj('SalaryComponentService', ['getById', 'create', 'update']);
      notificationSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
      routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      serviceSpy.getById.and.returnValue(of(mockComponent));

      TestBed.configureTestingModule({
        imports: [SalaryComponentFormComponent, HttpClientTestingModule, NoopAnimationsModule],
        providers: [
          { provide: SalaryComponentService, useValue: serviceSpy },
          { provide: NotificationService, useValue: notificationSpy },
          { provide: Router, useValue: routerSpy },
          { provide: ActivatedRoute, useValue: { params: of({}), snapshot: { paramMap: { get: () => '1' } } } }
        ]
      });

      fixture = TestBed.createComponent(SalaryComponentFormComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should be in edit mode', () => {
      expect(component.isEditMode).toBeTrue();
      expect(component.componentId).toBe(1);
    });

    it('should load component data', () => {
      expect(serviceSpy.getById).toHaveBeenCalledWith(1);
      expect(component.componentForm.get('name')?.value).toBe('Basic Salary');
    });

    it('should update component on submit', () => {
      serviceSpy.update.and.returnValue(of(mockComponent));
      component.onSubmit();
      expect(serviceSpy.update).toHaveBeenCalledWith(1, jasmine.any(Object));
      expect(notificationSpy.success).toHaveBeenCalledWith('Component updated successfully');
    });

    it('should handle update error', () => {
      serviceSpy.update.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationSpy.error).toHaveBeenCalledWith('Failed to update component');
    });

    it('should handle load error', () => {
      serviceSpy.getById.and.returnValue(throwError(() => new Error('fail')));
      component.loadComponent(999);
      expect(notificationSpy.error).toHaveBeenCalledWith('Failed to load salary component');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/salary-components']);
    });
  });

  function fillValidForm() {
    component.componentForm.patchValue({
      code: 'BASIC',
      name: 'Basic Salary',
      type: 'EARNING',
      computationType: 'FIXED',
      description: 'Basic salary'
    });
  }
});
