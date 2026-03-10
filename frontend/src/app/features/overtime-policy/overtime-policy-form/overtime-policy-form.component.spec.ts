import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { OvertimePolicyFormComponent } from './overtime-policy-form.component';
import { OvertimePolicyService } from '../../../services/overtime-policy.service';
import { NotificationService } from '../../../services/notification.service';
import { OvertimePolicyResponse } from '../../../models/overtime-policy.model';

describe('OvertimePolicyFormComponent', () => {
  let component: OvertimePolicyFormComponent;
  let fixture: ComponentFixture<OvertimePolicyFormComponent>;
  let overtimePolicyServiceSpy: jasmine.SpyObj<OvertimePolicyService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockPolicy: OvertimePolicyResponse = {
    id: 1, name: 'Weekday OT', type: 'WEEKDAY',
    rateMultiplier: 1.5, minOvertimeMinutes: 30,
    maxOvertimeMinutesPerDay: 120, maxOvertimeMinutesPerMonth: 2400,
    requiresApproval: true, active: true,
    description: 'Weekday overtime policy',
    createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
  };

  function setup(routeId: string | null = null) {
    overtimePolicyServiceSpy = jasmine.createSpyObj('OvertimePolicyService', ['getById', 'create', 'update']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [
        OvertimePolicyFormComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: OvertimePolicyService, useValue: overtimePolicyServiceSpy },
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

    fixture = TestBed.createComponent(OvertimePolicyFormComponent);
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
      expect(component.policyForm).toBeDefined();
    });

    it('should require name', () => {
      expect(component.policyForm.get('name')?.hasError('required')).toBeTrue();
    });

    it('should require type', () => {
      expect(component.policyForm.get('type')?.hasError('required')).toBeTrue();
    });

    it('should require rate multiplier', () => {
      expect(component.policyForm.get('rateMultiplier')?.hasError('required')).toBeTrue();
    });

    it('should default requires approval to true', () => {
      expect(component.policyForm.get('requiresApproval')?.value).toBeTrue();
    });

    it('should show error if submitting invalid form', () => {
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Please fill in all required fields correctly');
    });

    it('should create policy on valid submit', () => {
      fillValidForm();
      overtimePolicyServiceSpy.create.and.returnValue(of(mockPolicy));
      component.onSubmit();
      expect(overtimePolicyServiceSpy.create).toHaveBeenCalled();
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Overtime policy created successfully');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/overtime-policies']);
    });

    it('should handle create error', () => {
      fillValidForm();
      overtimePolicyServiceSpy.create.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to create overtime policy');
    });

    it('should navigate back on cancel', () => {
      component.onCancel();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/overtime-policies']);
    });
  });

  describe('edit mode', () => {
    beforeEach(() => {
      overtimePolicyServiceSpy = jasmine.createSpyObj('OvertimePolicyService', ['getById', 'create', 'update']);
      notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
      routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      overtimePolicyServiceSpy.getById.and.returnValue(of(mockPolicy));

      TestBed.configureTestingModule({
        imports: [OvertimePolicyFormComponent, HttpClientTestingModule, NoopAnimationsModule],
        providers: [
          { provide: OvertimePolicyService, useValue: overtimePolicyServiceSpy },
          { provide: NotificationService, useValue: notificationServiceSpy },
          { provide: Router, useValue: routerSpy },
          { provide: ActivatedRoute, useValue: { params: of({}), snapshot: { paramMap: { get: () => '1' } } } }
        ]
      });

      fixture = TestBed.createComponent(OvertimePolicyFormComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should be in edit mode', () => {
      expect(component.isEditMode).toBeTrue();
      expect(component.policyId).toBe(1);
    });

    it('should load policy data', () => {
      expect(overtimePolicyServiceSpy.getById).toHaveBeenCalledWith(1);
      expect(component.policyForm.get('name')?.value).toBe('Weekday OT');
    });

    it('should update policy on submit', () => {
      overtimePolicyServiceSpy.update.and.returnValue(of(mockPolicy));
      component.onSubmit();
      expect(overtimePolicyServiceSpy.update).toHaveBeenCalledWith(1, jasmine.any(Object));
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Overtime policy updated successfully');
    });

    it('should handle update error', () => {
      overtimePolicyServiceSpy.update.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to update overtime policy');
    });

    it('should handle load policy error', () => {
      overtimePolicyServiceSpy.getById.and.returnValue(throwError(() => new Error('fail')));
      component.loadPolicy(999);
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load overtime policy');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/overtime-policies']);
    });
  });

  function fillValidForm() {
    component.policyForm.patchValue({
      name: 'Weekday OT',
      type: 'WEEKDAY',
      rateMultiplier: 1.5,
      minOvertimeMinutes: 30,
      maxOvertimeMinutesPerDay: 120,
      maxOvertimeMinutesPerMonth: 2400,
      requiresApproval: true,
      description: 'Weekday overtime policy'
    });
  }
});
