import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ShiftRosterFormComponent } from './shift-roster-form.component';
import { ShiftRosterService } from '../../../services/shift-roster.service';
import { ShiftService } from '../../../services/shift.service';
import { RotationPatternService } from '../../../services/rotation-pattern.service';
import { NotificationService } from '../../../services/notification.service';
import { ShiftRosterResponse } from '../../../models/shift-roster.model';
import { Page } from '../../../models/page.model';
import { RotationPatternResponse } from '../../../models/rotation-pattern.model';

describe('ShiftRosterFormComponent', () => {
  let component: ShiftRosterFormComponent;
  let fixture: ComponentFixture<ShiftRosterFormComponent>;
  let shiftRosterServiceSpy: jasmine.SpyObj<ShiftRosterService>;
  let shiftServiceSpy: jasmine.SpyObj<ShiftService>;
  let rotationPatternServiceSpy: jasmine.SpyObj<RotationPatternService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockRoster: ShiftRosterResponse = {
    id: 1, employeeId: 1, employeeName: 'John Doe', employeeCode: 'EMP-001',
    shiftId: 1, shiftName: 'Morning Shift',
    effectiveDate: '2026-07-01', endDate: '2026-12-31',
    rotationPatternId: null, rotationPatternName: null,
    createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
  };

  const mockPatternPage: Page<RotationPatternResponse> = {
    content: [],
    totalElements: 0, totalPages: 0, size: 100, number: 0,
    first: true, last: true, empty: true
  };

  function setup(routeId: string | null = null) {
    shiftRosterServiceSpy = jasmine.createSpyObj('ShiftRosterService', ['getById', 'create', 'update']);
    shiftServiceSpy = jasmine.createSpyObj('ShiftService', ['getActive']);
    rotationPatternServiceSpy = jasmine.createSpyObj('RotationPatternService', ['getAll']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    shiftServiceSpy.getActive.and.returnValue(of([]));
    rotationPatternServiceSpy.getAll.and.returnValue(of(mockPatternPage));

    TestBed.configureTestingModule({
      imports: [
        ShiftRosterFormComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: ShiftRosterService, useValue: shiftRosterServiceSpy },
        { provide: ShiftService, useValue: shiftServiceSpy },
        { provide: RotationPatternService, useValue: rotationPatternServiceSpy },
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

    fixture = TestBed.createComponent(ShiftRosterFormComponent);
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
      expect(component.rosterForm).toBeDefined();
    });

    it('should require employee ID', () => {
      expect(component.rosterForm.get('employeeId')?.hasError('required')).toBeTrue();
    });

    it('should require shift ID', () => {
      expect(component.rosterForm.get('shiftId')?.hasError('required')).toBeTrue();
    });

    it('should require effective date', () => {
      expect(component.rosterForm.get('effectiveDate')?.hasError('required')).toBeTrue();
    });

    it('should show error if submitting invalid form', () => {
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Please fill in all required fields correctly');
    });

    it('should create roster on valid submit', () => {
      fillValidForm();
      shiftRosterServiceSpy.create.and.returnValue(of(mockRoster));
      component.onSubmit();
      expect(shiftRosterServiceSpy.create).toHaveBeenCalled();
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Roster assignment created successfully');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/shift-rosters']);
    });

    it('should handle create error', () => {
      fillValidForm();
      shiftRosterServiceSpy.create.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to create roster assignment');
    });

    it('should navigate back on cancel', () => {
      component.onCancel();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/shift-rosters']);
    });

    it('should load dropdown data on init', () => {
      expect(shiftServiceSpy.getActive).toHaveBeenCalled();
      expect(rotationPatternServiceSpy.getAll).toHaveBeenCalled();
    });
  });

  describe('edit mode', () => {
    beforeEach(() => {
      shiftRosterServiceSpy = jasmine.createSpyObj('ShiftRosterService', ['getById', 'create', 'update']);
      shiftServiceSpy = jasmine.createSpyObj('ShiftService', ['getActive']);
      rotationPatternServiceSpy = jasmine.createSpyObj('RotationPatternService', ['getAll']);
      notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
      routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      shiftRosterServiceSpy.getById.and.returnValue(of(mockRoster));
      shiftServiceSpy.getActive.and.returnValue(of([]));
      rotationPatternServiceSpy.getAll.and.returnValue(of(mockPatternPage));

      TestBed.configureTestingModule({
        imports: [ShiftRosterFormComponent, HttpClientTestingModule, NoopAnimationsModule],
        providers: [
          { provide: ShiftRosterService, useValue: shiftRosterServiceSpy },
          { provide: ShiftService, useValue: shiftServiceSpy },
          { provide: RotationPatternService, useValue: rotationPatternServiceSpy },
          { provide: NotificationService, useValue: notificationServiceSpy },
          { provide: Router, useValue: routerSpy },
          { provide: ActivatedRoute, useValue: { params: of({}), snapshot: { paramMap: { get: () => '1' } } } }
        ]
      });

      fixture = TestBed.createComponent(ShiftRosterFormComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should be in edit mode', () => {
      expect(component.isEditMode).toBeTrue();
      expect(component.rosterId).toBe(1);
    });

    it('should load roster data', () => {
      expect(shiftRosterServiceSpy.getById).toHaveBeenCalledWith(1);
      expect(component.rosterForm.get('employeeId')?.value).toBe(1);
    });

    it('should update roster on submit', () => {
      shiftRosterServiceSpy.update.and.returnValue(of(mockRoster));
      component.onSubmit();
      expect(shiftRosterServiceSpy.update).toHaveBeenCalledWith(1, jasmine.any(Object));
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Roster assignment updated successfully');
    });

    it('should handle update error', () => {
      shiftRosterServiceSpy.update.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to update roster assignment');
    });

    it('should handle load roster error', () => {
      shiftRosterServiceSpy.getById.and.returnValue(throwError(() => new Error('fail')));
      component.loadRoster(999);
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load roster assignment');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/shift-rosters']);
    });
  });

  function fillValidForm() {
    component.rosterForm.patchValue({
      employeeId: 1,
      shiftId: 1,
      effectiveDate: '2026-07-01',
      endDate: '2026-12-31'
    });
  }
});
