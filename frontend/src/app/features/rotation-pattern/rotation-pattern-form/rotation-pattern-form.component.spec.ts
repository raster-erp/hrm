import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { RotationPatternFormComponent } from './rotation-pattern-form.component';
import { RotationPatternService } from '../../../services/rotation-pattern.service';
import { NotificationService } from '../../../services/notification.service';
import { RotationPatternResponse } from '../../../models/rotation-pattern.model';

describe('RotationPatternFormComponent', () => {
  let component: RotationPatternFormComponent;
  let fixture: ComponentFixture<RotationPatternFormComponent>;
  let rotationPatternServiceSpy: jasmine.SpyObj<RotationPatternService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockPattern: RotationPatternResponse = {
    id: 1, name: 'Weekly Rotation', description: 'Rotates weekly',
    rotationDays: 7, shiftSequence: '1,2,3,1,2,3,1',
    createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
  };

  function setup(routeId: string | null = null) {
    rotationPatternServiceSpy = jasmine.createSpyObj('RotationPatternService', ['getById', 'create', 'update']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [
        RotationPatternFormComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
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

    fixture = TestBed.createComponent(RotationPatternFormComponent);
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
      expect(component.patternForm).toBeDefined();
    });

    it('should require name', () => {
      expect(component.patternForm.get('name')?.hasError('required')).toBeTrue();
    });

    it('should require shift sequence', () => {
      expect(component.patternForm.get('shiftSequence')?.hasError('required')).toBeTrue();
    });

    it('should show error if submitting invalid form', () => {
      component.patternForm.patchValue({ name: '', shiftSequence: '' });
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Please fill in all required fields correctly');
    });

    it('should create pattern on valid submit', () => {
      fillValidForm();
      rotationPatternServiceSpy.create.and.returnValue(of(mockPattern));
      component.onSubmit();
      expect(rotationPatternServiceSpy.create).toHaveBeenCalled();
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Rotation pattern created successfully');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/rotation-patterns']);
    });

    it('should handle create error', () => {
      fillValidForm();
      rotationPatternServiceSpy.create.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to create rotation pattern');
    });

    it('should navigate back on cancel', () => {
      component.onCancel();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/rotation-patterns']);
    });
  });

  describe('edit mode', () => {
    beforeEach(() => {
      rotationPatternServiceSpy = jasmine.createSpyObj('RotationPatternService', ['getById', 'create', 'update']);
      notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
      routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      rotationPatternServiceSpy.getById.and.returnValue(of(mockPattern));

      TestBed.configureTestingModule({
        imports: [RotationPatternFormComponent, HttpClientTestingModule, NoopAnimationsModule],
        providers: [
          { provide: RotationPatternService, useValue: rotationPatternServiceSpy },
          { provide: NotificationService, useValue: notificationServiceSpy },
          { provide: Router, useValue: routerSpy },
          { provide: ActivatedRoute, useValue: { params: of({}), snapshot: { paramMap: { get: () => '1' } } } }
        ]
      });

      fixture = TestBed.createComponent(RotationPatternFormComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should be in edit mode', () => {
      expect(component.isEditMode).toBeTrue();
      expect(component.patternId).toBe(1);
    });

    it('should load pattern data', () => {
      expect(rotationPatternServiceSpy.getById).toHaveBeenCalledWith(1);
      expect(component.patternForm.get('name')?.value).toBe('Weekly Rotation');
    });

    it('should update pattern on submit', () => {
      rotationPatternServiceSpy.update.and.returnValue(of(mockPattern));
      component.onSubmit();
      expect(rotationPatternServiceSpy.update).toHaveBeenCalledWith(1, jasmine.any(Object));
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Rotation pattern updated successfully');
    });

    it('should handle update error', () => {
      rotationPatternServiceSpy.update.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to update rotation pattern');
    });

    it('should handle load pattern error', () => {
      rotationPatternServiceSpy.getById.and.returnValue(throwError(() => new Error('fail')));
      component.loadPattern(999);
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load rotation pattern');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/rotation-patterns']);
    });
  });

  function fillValidForm() {
    component.patternForm.patchValue({
      name: 'Weekly Rotation',
      description: 'Rotates weekly',
      rotationDays: 7,
      shiftSequence: '1,2,3,1,2,3,1'
    });
  }
});
