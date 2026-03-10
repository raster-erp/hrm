import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ShiftFormComponent } from './shift-form.component';
import { ShiftService } from '../../../services/shift.service';
import { NotificationService } from '../../../services/notification.service';
import { ShiftResponse } from '../../../models/shift.model';

describe('ShiftFormComponent', () => {
  let component: ShiftFormComponent;
  let fixture: ComponentFixture<ShiftFormComponent>;
  let shiftServiceSpy: jasmine.SpyObj<ShiftService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockShift: ShiftResponse = {
    id: 1, name: 'Morning Shift', type: 'MORNING',
    startTime: '06:00', endTime: '14:00',
    breakDurationMinutes: 30, gracePeriodMinutes: 10,
    description: 'Morning shift', active: true,
    createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
  };

  function setup(routeId: string | null = null) {
    shiftServiceSpy = jasmine.createSpyObj('ShiftService', ['getById', 'create', 'update']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [
        ShiftFormComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: ShiftService, useValue: shiftServiceSpy },
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

    fixture = TestBed.createComponent(ShiftFormComponent);
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
      expect(component.shiftForm).toBeDefined();
    });

    it('should require name', () => {
      expect(component.shiftForm.get('name')?.hasError('required')).toBeTrue();
    });

    it('should require type', () => {
      expect(component.shiftForm.get('type')?.hasError('required')).toBeTrue();
    });

    it('should require start time', () => {
      expect(component.shiftForm.get('startTime')?.hasError('required')).toBeTrue();
    });

    it('should require end time', () => {
      expect(component.shiftForm.get('endTime')?.hasError('required')).toBeTrue();
    });

    it('should show error if submitting invalid form', () => {
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Please fill in all required fields correctly');
    });

    it('should create shift on valid submit', () => {
      fillValidForm();
      shiftServiceSpy.create.and.returnValue(of(mockShift));
      component.onSubmit();
      expect(shiftServiceSpy.create).toHaveBeenCalled();
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Shift created successfully');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/shifts']);
    });

    it('should handle create error', () => {
      fillValidForm();
      shiftServiceSpy.create.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to create shift');
    });

    it('should navigate back on cancel', () => {
      component.onCancel();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/shifts']);
    });
  });

  describe('edit mode', () => {
    beforeEach(() => {
      shiftServiceSpy = jasmine.createSpyObj('ShiftService', ['getById', 'create', 'update']);
      notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
      routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      shiftServiceSpy.getById.and.returnValue(of(mockShift));

      TestBed.configureTestingModule({
        imports: [ShiftFormComponent, HttpClientTestingModule, NoopAnimationsModule],
        providers: [
          { provide: ShiftService, useValue: shiftServiceSpy },
          { provide: NotificationService, useValue: notificationServiceSpy },
          { provide: Router, useValue: routerSpy },
          { provide: ActivatedRoute, useValue: { params: of({}), snapshot: { paramMap: { get: () => '1' } } } }
        ]
      });

      fixture = TestBed.createComponent(ShiftFormComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should be in edit mode', () => {
      expect(component.isEditMode).toBeTrue();
      expect(component.shiftId).toBe(1);
    });

    it('should load shift data', () => {
      expect(shiftServiceSpy.getById).toHaveBeenCalledWith(1);
      expect(component.shiftForm.get('name')?.value).toBe('Morning Shift');
    });

    it('should update shift on submit', () => {
      shiftServiceSpy.update.and.returnValue(of(mockShift));
      component.onSubmit();
      expect(shiftServiceSpy.update).toHaveBeenCalledWith(1, jasmine.any(Object));
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Shift updated successfully');
    });

    it('should handle update error', () => {
      shiftServiceSpy.update.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to update shift');
    });

    it('should handle load shift error', () => {
      shiftServiceSpy.getById.and.returnValue(throwError(() => new Error('fail')));
      component.loadShift(999);
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load shift');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/shifts']);
    });
  });

  function fillValidForm() {
    component.shiftForm.patchValue({
      name: 'Morning Shift',
      type: 'MORNING',
      startTime: '06:00',
      endTime: '14:00',
      breakDurationMinutes: 30,
      gracePeriodMinutes: 10,
      description: 'Morning shift'
    });
  }
});
