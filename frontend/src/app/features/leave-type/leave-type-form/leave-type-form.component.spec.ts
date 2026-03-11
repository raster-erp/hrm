import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LeaveTypeFormComponent } from './leave-type-form.component';
import { LeaveTypeService } from '../../../services/leave-type.service';
import { NotificationService } from '../../../services/notification.service';
import { LeaveTypeResponse } from '../../../models/leave-type.model';

describe('LeaveTypeFormComponent', () => {
  let component: LeaveTypeFormComponent;
  let fixture: ComponentFixture<LeaveTypeFormComponent>;
  let leaveTypeServiceSpy: jasmine.SpyObj<LeaveTypeService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockLeaveType: LeaveTypeResponse = {
    id: 1, code: 'CL', name: 'Casual Leave', category: 'PAID',
    description: 'Casual leave', active: true,
    createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
  };

  function setup(routeId: string | null = null) {
    leaveTypeServiceSpy = jasmine.createSpyObj('LeaveTypeService', ['getById', 'create', 'update']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [
        LeaveTypeFormComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
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

    fixture = TestBed.createComponent(LeaveTypeFormComponent);
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
      expect(component.leaveTypeForm).toBeDefined();
    });

    it('should require code', () => {
      expect(component.leaveTypeForm.get('code')?.hasError('required')).toBeTrue();
    });

    it('should require name', () => {
      expect(component.leaveTypeForm.get('name')?.hasError('required')).toBeTrue();
    });

    it('should require category', () => {
      expect(component.leaveTypeForm.get('category')?.hasError('required')).toBeTrue();
    });

    it('should show error if submitting invalid form', () => {
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Please fill in all required fields correctly');
    });

    it('should create leave type on valid submit', () => {
      fillValidForm();
      leaveTypeServiceSpy.create.and.returnValue(of(mockLeaveType));
      component.onSubmit();
      expect(leaveTypeServiceSpy.create).toHaveBeenCalled();
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Leave type created successfully');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/leave-types']);
    });

    it('should handle create error', () => {
      fillValidForm();
      leaveTypeServiceSpy.create.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to create leave type');
    });

    it('should navigate back on cancel', () => {
      component.onCancel();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/leave-types']);
    });
  });

  describe('edit mode', () => {
    beforeEach(() => {
      leaveTypeServiceSpy = jasmine.createSpyObj('LeaveTypeService', ['getById', 'create', 'update']);
      notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
      routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      leaveTypeServiceSpy.getById.and.returnValue(of(mockLeaveType));

      TestBed.configureTestingModule({
        imports: [LeaveTypeFormComponent, HttpClientTestingModule, NoopAnimationsModule],
        providers: [
          { provide: LeaveTypeService, useValue: leaveTypeServiceSpy },
          { provide: NotificationService, useValue: notificationServiceSpy },
          { provide: Router, useValue: routerSpy },
          { provide: ActivatedRoute, useValue: { params: of({}), snapshot: { paramMap: { get: () => '1' } } } }
        ]
      });

      fixture = TestBed.createComponent(LeaveTypeFormComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should be in edit mode', () => {
      expect(component.isEditMode).toBeTrue();
      expect(component.leaveTypeId).toBe(1);
    });

    it('should load leave type data', () => {
      expect(leaveTypeServiceSpy.getById).toHaveBeenCalledWith(1);
      expect(component.leaveTypeForm.get('name')?.value).toBe('Casual Leave');
    });

    it('should update leave type on submit', () => {
      leaveTypeServiceSpy.update.and.returnValue(of(mockLeaveType));
      component.onSubmit();
      expect(leaveTypeServiceSpy.update).toHaveBeenCalledWith(1, jasmine.any(Object));
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Leave type updated successfully');
    });

    it('should handle update error', () => {
      leaveTypeServiceSpy.update.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to update leave type');
    });

    it('should handle load leave type error', () => {
      leaveTypeServiceSpy.getById.and.returnValue(throwError(() => new Error('fail')));
      component.loadLeaveType(999);
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load leave type');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/leave-types']);
    });
  });

  function fillValidForm() {
    component.leaveTypeForm.patchValue({
      code: 'CL',
      name: 'Casual Leave',
      category: 'PAID',
      description: 'Casual leave'
    });
  }
});
