import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LeavePolicyFormComponent } from './leave-policy-form.component';
import { LeavePolicyService } from '../../../services/leave-policy.service';
import { LeaveTypeService } from '../../../services/leave-type.service';
import { NotificationService } from '../../../services/notification.service';
import { LeavePolicyResponse } from '../../../models/leave-policy.model';

describe('LeavePolicyFormComponent', () => {
  let component: LeavePolicyFormComponent;
  let fixture: ComponentFixture<LeavePolicyFormComponent>;
  let leavePolicyServiceSpy: jasmine.SpyObj<LeavePolicyService>;
  let leaveTypeServiceSpy: jasmine.SpyObj<LeaveTypeService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockPolicy: LeavePolicyResponse = {
    id: 1, name: 'Standard CL Policy', leaveTypeId: 1,
    leaveTypeName: 'Casual Leave', leaveTypeCode: 'CL',
    accrualFrequency: 'MONTHLY', accrualDays: 1.5,
    maxAccumulation: 12, carryForwardLimit: 5,
    proRataForNewJoiners: true, minServiceDaysRequired: 0,
    active: true, description: 'Standard casual leave policy',
    createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
  };

  function setup(routeId: string | null = null) {
    leavePolicyServiceSpy = jasmine.createSpyObj('LeavePolicyService', ['getById', 'create', 'update']);
    leaveTypeServiceSpy = jasmine.createSpyObj('LeaveTypeService', ['getActive']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    leaveTypeServiceSpy.getActive.and.returnValue(of([]));

    TestBed.configureTestingModule({
      imports: [
        LeavePolicyFormComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: LeavePolicyService, useValue: leavePolicyServiceSpy },
        { provide: LeaveTypeService, useValue: leaveTypeServiceSpy },
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

    fixture = TestBed.createComponent(LeavePolicyFormComponent);
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

    it('should require leave type', () => {
      expect(component.policyForm.get('leaveTypeId')?.hasError('required')).toBeTrue();
    });

    it('should require accrual frequency', () => {
      expect(component.policyForm.get('accrualFrequency')?.hasError('required')).toBeTrue();
    });

    it('should require accrual days', () => {
      expect(component.policyForm.get('accrualDays')?.hasError('required')).toBeTrue();
    });

    it('should default pro-rata to false', () => {
      expect(component.policyForm.get('proRataForNewJoiners')?.value).toBeFalse();
    });

    it('should load leave types on init', () => {
      expect(leaveTypeServiceSpy.getActive).toHaveBeenCalled();
    });

    it('should show error if submitting invalid form', () => {
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Please fill in all required fields correctly');
    });

    it('should create policy on valid submit', () => {
      fillValidForm();
      leavePolicyServiceSpy.create.and.returnValue(of(mockPolicy));
      component.onSubmit();
      expect(leavePolicyServiceSpy.create).toHaveBeenCalled();
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Leave policy created successfully');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/leave-policies']);
    });

    it('should handle create error', () => {
      fillValidForm();
      leavePolicyServiceSpy.create.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to create leave policy');
    });

    it('should navigate back on cancel', () => {
      component.onCancel();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/leave-policies']);
    });
  });

  describe('edit mode', () => {
    beforeEach(() => {
      leavePolicyServiceSpy = jasmine.createSpyObj('LeavePolicyService', ['getById', 'create', 'update']);
      leaveTypeServiceSpy = jasmine.createSpyObj('LeaveTypeService', ['getActive']);
      notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
      routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      leavePolicyServiceSpy.getById.and.returnValue(of(mockPolicy));
      leaveTypeServiceSpy.getActive.and.returnValue(of([]));

      TestBed.configureTestingModule({
        imports: [LeavePolicyFormComponent, HttpClientTestingModule, NoopAnimationsModule],
        providers: [
          { provide: LeavePolicyService, useValue: leavePolicyServiceSpy },
          { provide: LeaveTypeService, useValue: leaveTypeServiceSpy },
          { provide: NotificationService, useValue: notificationServiceSpy },
          { provide: Router, useValue: routerSpy },
          { provide: ActivatedRoute, useValue: { params: of({}), snapshot: { paramMap: { get: () => '1' } } } }
        ]
      });

      fixture = TestBed.createComponent(LeavePolicyFormComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should be in edit mode', () => {
      expect(component.isEditMode).toBeTrue();
      expect(component.policyId).toBe(1);
    });

    it('should load policy data', () => {
      expect(leavePolicyServiceSpy.getById).toHaveBeenCalledWith(1);
      expect(component.policyForm.get('name')?.value).toBe('Standard CL Policy');
    });

    it('should update policy on submit', () => {
      leavePolicyServiceSpy.update.and.returnValue(of(mockPolicy));
      component.onSubmit();
      expect(leavePolicyServiceSpy.update).toHaveBeenCalledWith(1, jasmine.any(Object));
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Leave policy updated successfully');
    });

    it('should handle update error', () => {
      leavePolicyServiceSpy.update.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to update leave policy');
    });

    it('should handle load policy error', () => {
      leavePolicyServiceSpy.getById.and.returnValue(throwError(() => new Error('fail')));
      component.loadPolicy(999);
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load leave policy');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/leave-policies']);
    });
  });

  function fillValidForm() {
    component.policyForm.patchValue({
      name: 'Standard CL Policy',
      leaveTypeId: 1,
      accrualFrequency: 'MONTHLY',
      accrualDays: 1.5,
      maxAccumulation: 12,
      carryForwardLimit: 5,
      proRataForNewJoiners: true,
      minServiceDaysRequired: 0,
      description: 'Standard casual leave policy'
    });
  }
});
