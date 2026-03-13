import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { InvestmentDeclarationFormComponent } from './investment-declaration-form.component';
import { InvestmentDeclarationService } from '../../../services/investment-declaration.service';
import { NotificationService } from '../../../services/notification.service';
import { InvestmentDeclarationResponse } from '../../../models/investment-declaration.model';

describe('InvestmentDeclarationFormComponent', () => {
  let component: InvestmentDeclarationFormComponent;
  let fixture: ComponentFixture<InvestmentDeclarationFormComponent>;
  let serviceSpy: jasmine.SpyObj<InvestmentDeclarationService>;
  let notificationSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockDeclaration: InvestmentDeclarationResponse = {
    id: 1, employeeId: 101, employeeName: 'John Doe',
    financialYear: '2025-26', regime: 'OLD',
    totalDeclaredAmount: 200000, totalVerifiedAmount: 0,
    status: 'DRAFT', remarks: 'Test remarks',
    submittedAt: null, verifiedAt: null, verifiedBy: null,
    items: [
      {
        id: 10, section: '80C', description: 'PPF',
        declaredAmount: 150000, verifiedAmount: 0,
        proofStatus: 'PENDING', proofDocumentName: null, proofRemarks: null,
        createdAt: '2026-01-01T00:00:00', updatedAt: '2026-01-01T00:00:00'
      }
    ],
    createdAt: '2026-01-01T00:00:00', updatedAt: '2026-01-01T00:00:00'
  };

  function setup(routeId: string | null = null) {
    serviceSpy = jasmine.createSpyObj('InvestmentDeclarationService', ['getById', 'create', 'update']);
    notificationSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [
        InvestmentDeclarationFormComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: InvestmentDeclarationService, useValue: serviceSpy },
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

    fixture = TestBed.createComponent(InvestmentDeclarationFormComponent);
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
      expect(component.declarationForm).toBeDefined();
    });

    it('should require employeeId', () => {
      expect(component.declarationForm.get('employeeId')?.hasError('required')).toBeTrue();
    });

    it('should require financialYear', () => {
      expect(component.declarationForm.get('financialYear')?.hasError('required')).toBeTrue();
    });

    it('should default regime to NEW', () => {
      expect(component.declarationForm.get('regime')?.value).toBe('NEW');
    });

    it('should start with empty items', () => {
      expect(component.items.length).toBe(0);
    });

    it('should add an item', () => {
      component.addItem();
      expect(component.items.length).toBe(1);
    });

    it('should remove an item', () => {
      component.addItem();
      component.addItem();
      component.removeItem(0);
      expect(component.items.length).toBe(1);
    });

    it('should show error if submitting invalid form', () => {
      component.onSubmit();
      expect(notificationSpy.error).toHaveBeenCalledWith('Please fill in all required fields correctly');
    });

    it('should create declaration on valid submit', () => {
      fillValidForm();
      serviceSpy.create.and.returnValue(of(mockDeclaration));
      component.onSubmit();
      expect(serviceSpy.create).toHaveBeenCalled();
      expect(notificationSpy.success).toHaveBeenCalledWith('Declaration created successfully');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/investment-declarations']);
    });

    it('should handle create error', () => {
      fillValidForm();
      serviceSpy.create.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationSpy.error).toHaveBeenCalledWith('Failed to create declaration');
    });

    it('should navigate back on cancel', () => {
      component.onCancel();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/investment-declarations']);
    });
  });

  describe('edit mode', () => {
    beforeEach(() => {
      serviceSpy = jasmine.createSpyObj('InvestmentDeclarationService', ['getById', 'create', 'update']);
      notificationSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
      routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      serviceSpy.getById.and.returnValue(of(mockDeclaration));

      TestBed.configureTestingModule({
        imports: [InvestmentDeclarationFormComponent, HttpClientTestingModule, NoopAnimationsModule],
        providers: [
          { provide: InvestmentDeclarationService, useValue: serviceSpy },
          { provide: NotificationService, useValue: notificationSpy },
          { provide: Router, useValue: routerSpy },
          { provide: ActivatedRoute, useValue: { params: of({}), snapshot: { paramMap: { get: () => '1' } } } }
        ]
      });

      fixture = TestBed.createComponent(InvestmentDeclarationFormComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should be in edit mode', () => {
      expect(component.isEditMode).toBeTrue();
      expect(component.declarationId).toBe(1);
    });

    it('should load declaration data', () => {
      expect(serviceSpy.getById).toHaveBeenCalledWith(1);
      expect(component.declarationForm.get('employeeId')?.value).toBe(101);
    });

    it('should load items from declaration', () => {
      expect(component.items.length).toBe(1);
    });

    it('should update declaration on submit', () => {
      serviceSpy.update.and.returnValue(of(mockDeclaration));
      component.onSubmit();
      expect(serviceSpy.update).toHaveBeenCalledWith(1, jasmine.any(Object));
      expect(notificationSpy.success).toHaveBeenCalledWith('Declaration updated successfully');
    });

    it('should handle update error', () => {
      serviceSpy.update.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationSpy.error).toHaveBeenCalledWith('Failed to update declaration');
    });

    it('should handle load error', () => {
      serviceSpy.getById.and.returnValue(throwError(() => new Error('fail')));
      component.loadDeclaration(999);
      expect(notificationSpy.error).toHaveBeenCalledWith('Failed to load declaration');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/investment-declarations']);
    });
  });

  function fillValidForm() {
    component.declarationForm.patchValue({
      employeeId: 101,
      financialYear: '2025-26',
      regime: 'OLD',
      remarks: 'Test'
    });
  }
});
