import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { TaxSlabFormComponent } from './tax-slab-form.component';
import { TaxSlabService } from '../../../services/tax-slab.service';
import { NotificationService } from '../../../services/notification.service';
import { TaxSlabResponse } from '../../../models/tax-slab.model';

describe('TaxSlabFormComponent', () => {
  let component: TaxSlabFormComponent;
  let fixture: ComponentFixture<TaxSlabFormComponent>;
  let serviceSpy: jasmine.SpyObj<TaxSlabService>;
  let notificationSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockSlab: TaxSlabResponse = {
    id: 1, regime: 'NEW', financialYear: '2025-26',
    slabFrom: 0, slabTo: 300000, rate: 0,
    description: 'No tax', active: true,
    createdAt: '2026-01-01T00:00:00', updatedAt: '2026-01-01T00:00:00'
  };

  function setup(routeId: string | null = null) {
    serviceSpy = jasmine.createSpyObj('TaxSlabService', ['getById', 'create', 'update']);
    notificationSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [
        TaxSlabFormComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: TaxSlabService, useValue: serviceSpy },
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

    fixture = TestBed.createComponent(TaxSlabFormComponent);
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
      expect(component.slabForm).toBeDefined();
    });

    it('should require regime', () => {
      expect(component.slabForm.get('regime')?.hasError('required')).toBeTrue();
    });

    it('should require financialYear', () => {
      expect(component.slabForm.get('financialYear')?.hasError('required')).toBeTrue();
    });

    it('should require slabFrom', () => {
      expect(component.slabForm.get('slabFrom')?.hasError('required')).toBeTrue();
    });

    it('should require rate', () => {
      expect(component.slabForm.get('rate')?.hasError('required')).toBeTrue();
    });

    it('should show error if submitting invalid form', () => {
      component.onSubmit();
      expect(notificationSpy.error).toHaveBeenCalledWith('Please fill in all required fields correctly');
    });

    it('should create tax slab on valid submit', () => {
      fillValidForm();
      serviceSpy.create.and.returnValue(of(mockSlab));
      component.onSubmit();
      expect(serviceSpy.create).toHaveBeenCalled();
      expect(notificationSpy.success).toHaveBeenCalledWith('Tax slab created successfully');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/tax-slabs']);
    });

    it('should handle create error', () => {
      fillValidForm();
      serviceSpy.create.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationSpy.error).toHaveBeenCalledWith('Failed to create tax slab');
    });

    it('should navigate back on cancel', () => {
      component.onCancel();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/tax-slabs']);
    });
  });

  describe('edit mode', () => {
    beforeEach(() => {
      serviceSpy = jasmine.createSpyObj('TaxSlabService', ['getById', 'create', 'update']);
      notificationSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
      routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      serviceSpy.getById.and.returnValue(of(mockSlab));

      TestBed.configureTestingModule({
        imports: [TaxSlabFormComponent, HttpClientTestingModule, NoopAnimationsModule],
        providers: [
          { provide: TaxSlabService, useValue: serviceSpy },
          { provide: NotificationService, useValue: notificationSpy },
          { provide: Router, useValue: routerSpy },
          { provide: ActivatedRoute, useValue: { params: of({}), snapshot: { paramMap: { get: () => '1' } } } }
        ]
      });

      fixture = TestBed.createComponent(TaxSlabFormComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should be in edit mode', () => {
      expect(component.isEditMode).toBeTrue();
      expect(component.slabId).toBe(1);
    });

    it('should load slab data', () => {
      expect(serviceSpy.getById).toHaveBeenCalledWith(1);
      expect(component.slabForm.get('regime')?.value).toBe('NEW');
    });

    it('should update slab on submit', () => {
      serviceSpy.update.and.returnValue(of(mockSlab));
      component.onSubmit();
      expect(serviceSpy.update).toHaveBeenCalledWith(1, jasmine.any(Object));
      expect(notificationSpy.success).toHaveBeenCalledWith('Tax slab updated successfully');
    });

    it('should handle update error', () => {
      serviceSpy.update.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationSpy.error).toHaveBeenCalledWith('Failed to update tax slab');
    });

    it('should handle load error', () => {
      serviceSpy.getById.and.returnValue(throwError(() => new Error('fail')));
      component.loadSlab(999);
      expect(notificationSpy.error).toHaveBeenCalledWith('Failed to load tax slab');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/tax-slabs']);
    });
  });

  function fillValidForm() {
    component.slabForm.patchValue({
      regime: 'NEW',
      financialYear: '2025-26',
      slabFrom: 0,
      slabTo: 300000,
      rate: 0,
      description: 'No tax'
    });
  }
});
