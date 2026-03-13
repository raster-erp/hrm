import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { ProofSubmissionComponent } from './proof-submission.component';
import { InvestmentDeclarationService } from '../../../services/investment-declaration.service';
import { NotificationService } from '../../../services/notification.service';
import { InvestmentDeclarationResponse } from '../../../models/investment-declaration.model';

describe('ProofSubmissionComponent', () => {
  let component: ProofSubmissionComponent;
  let fixture: ComponentFixture<ProofSubmissionComponent>;
  let serviceSpy: jasmine.SpyObj<InvestmentDeclarationService>;
  let notificationSpy: jasmine.SpyObj<NotificationService>;

  const mockDeclaration: InvestmentDeclarationResponse = {
    id: 1, employeeId: 101, employeeName: 'John Doe',
    financialYear: '2025-26', regime: 'OLD',
    totalDeclaredAmount: 200000, totalVerifiedAmount: 0,
    status: 'SUBMITTED', remarks: null,
    submittedAt: '2026-01-15T00:00:00', verifiedAt: null, verifiedBy: null,
    items: [{
      id: 10, section: '80C', description: 'PPF Investment',
      declaredAmount: 150000, verifiedAmount: 0,
      proofStatus: 'PENDING', proofDocumentName: null, proofRemarks: null,
      createdAt: '2026-01-01T00:00:00', updatedAt: '2026-01-01T00:00:00'
    }],
    createdAt: '2026-01-01T00:00:00', updatedAt: '2026-01-01T00:00:00'
  };

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('InvestmentDeclarationService', [
      'getById', 'submitProof', 'verifyProof'
    ]);
    notificationSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    serviceSpy.getById.and.returnValue(of(mockDeclaration));
    serviceSpy.submitProof.and.returnValue(of(void 0));
    serviceSpy.verifyProof.and.returnValue(of(void 0));

    await TestBed.configureTestingModule({
      imports: [
        ProofSubmissionComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: InvestmentDeclarationService, useValue: serviceSpy },
        { provide: NotificationService, useValue: notificationSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProofSubmissionComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load declaration by ID', () => {
    component.searchId = '1';
    component.loadDeclaration();
    expect(serviceSpy.getById).toHaveBeenCalledWith(1);
    expect(component.declaration).toEqual(mockDeclaration);
    expect(component.dataSource.data.length).toBe(1);
    expect(component.loading).toBeFalse();
  });

  it('should handle load error', () => {
    serviceSpy.getById.and.returnValue(throwError(() => new Error('fail')));
    component.searchId = '1';
    component.loadDeclaration();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to load declaration');
    expect(component.declaration).toBeNull();
    expect(component.dataSource.data.length).toBe(0);
  });

  it('should show error when search input is empty', () => {
    component.searchId = '';
    component.loadDeclaration();
    expect(notificationSpy.error).toHaveBeenCalledWith('Please enter a valid Declaration ID');
    expect(serviceSpy.getById).not.toHaveBeenCalled();
  });

  it('should submit proof for an item', () => {
    component.searchId = '1';
    component.loadDeclaration();
    const item = component.dataSource.data[0];

    component.openProofForm(item);
    component.proofForm.patchValue({
      proofDocumentName: 'PPF_Receipt.pdf',
      declaredAmount: 150000
    });

    component.submitProof(item);
    expect(serviceSpy.submitProof).toHaveBeenCalledWith({
      itemId: 10,
      proofDocumentName: 'PPF_Receipt.pdf',
      declaredAmount: 150000
    });
    expect(notificationSpy.success).toHaveBeenCalledWith('Proof submitted successfully');
    expect(component.activeProofItemId).toBeNull();
  });

  it('should handle submit proof error', () => {
    serviceSpy.submitProof.and.returnValue(throwError(() => new Error('fail')));
    component.searchId = '1';
    component.loadDeclaration();
    const item = component.dataSource.data[0];

    component.openProofForm(item);
    component.proofForm.patchValue({
      proofDocumentName: 'PPF_Receipt.pdf',
      declaredAmount: 150000
    });

    component.submitProof(item);
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to submit proof');
  });

  it('should verify proof for an item', () => {
    const submittedDeclaration: InvestmentDeclarationResponse = {
      ...mockDeclaration,
      items: [{
        ...mockDeclaration.items[0],
        proofStatus: 'SUBMITTED'
      }]
    };
    serviceSpy.getById.and.returnValue(of(submittedDeclaration));

    component.searchId = '1';
    component.loadDeclaration();
    const item = component.dataSource.data[0];

    component.openVerifyForm(item);
    component.verifyForm.patchValue({
      verifiedAmount: 140000,
      status: 'VERIFIED',
      remarks: 'All good'
    });

    component.verifyProof(item);
    expect(serviceSpy.verifyProof).toHaveBeenCalledWith({
      itemId: 10,
      verifiedAmount: 140000,
      status: 'VERIFIED',
      remarks: 'All good'
    });
    expect(notificationSpy.success).toHaveBeenCalledWith('Proof verified successfully');
    expect(component.activeVerifyItemId).toBeNull();
  });

  it('should correctly determine canSubmitProof', () => {
    const pendingItem = { ...mockDeclaration.items[0], proofStatus: 'PENDING' };
    const submittedItem = { ...mockDeclaration.items[0], proofStatus: 'SUBMITTED' };
    const verifiedItem = { ...mockDeclaration.items[0], proofStatus: 'VERIFIED' };
    const rejectedItem = { ...mockDeclaration.items[0], proofStatus: 'REJECTED' };

    expect(component.canSubmitProof(pendingItem)).toBeTrue();
    expect(component.canSubmitProof(rejectedItem)).toBeTrue();
    expect(component.canSubmitProof(submittedItem)).toBeFalse();
    expect(component.canSubmitProof(verifiedItem)).toBeFalse();
  });

  it('should correctly determine canVerifyProof', () => {
    const pendingItem = { ...mockDeclaration.items[0], proofStatus: 'PENDING' };
    const submittedItem = { ...mockDeclaration.items[0], proofStatus: 'SUBMITTED' };
    const verifiedItem = { ...mockDeclaration.items[0], proofStatus: 'VERIFIED' };

    expect(component.canVerifyProof(submittedItem)).toBeTrue();
    expect(component.canVerifyProof(pendingItem)).toBeFalse();
    expect(component.canVerifyProof(verifiedItem)).toBeFalse();
  });

  it('should open and cancel proof form', () => {
    const item = mockDeclaration.items[0];
    component.openProofForm(item);
    expect(component.activeProofItemId).toBe(10);
    expect(component.activeVerifyItemId).toBeNull();
    expect(component.proofForm.value.declaredAmount).toBe(150000);

    component.cancelProofForm();
    expect(component.activeProofItemId).toBeNull();
  });

  it('should open and cancel verify form', () => {
    const item = mockDeclaration.items[0];
    component.openVerifyForm(item);
    expect(component.activeVerifyItemId).toBe(10);
    expect(component.activeProofItemId).toBeNull();
    expect(component.verifyForm.value.verifiedAmount).toBe(150000);

    component.cancelVerifyForm();
    expect(component.activeVerifyItemId).toBeNull();
  });

  it('should not submit proof when form is invalid', () => {
    component.searchId = '1';
    component.loadDeclaration();
    const item = component.dataSource.data[0];

    component.openProofForm(item);
    component.proofForm.patchValue({ proofDocumentName: '', declaredAmount: null });

    component.submitProof(item);
    expect(serviceSpy.submitProof).not.toHaveBeenCalled();
  });

  it('should not verify proof when form is invalid', () => {
    component.searchId = '1';
    component.loadDeclaration();
    const item = component.dataSource.data[0];

    component.openVerifyForm(item);
    component.verifyForm.patchValue({ verifiedAmount: null, status: '', remarks: '' });

    component.verifyProof(item);
    expect(serviceSpy.verifyProof).not.toHaveBeenCalled();
  });

  it('should handle verify proof error', () => {
    serviceSpy.verifyProof.and.returnValue(throwError(() => new Error('fail')));
    const submittedDeclaration: InvestmentDeclarationResponse = {
      ...mockDeclaration,
      items: [{
        ...mockDeclaration.items[0],
        proofStatus: 'SUBMITTED'
      }]
    };
    serviceSpy.getById.and.returnValue(of(submittedDeclaration));

    component.searchId = '1';
    component.loadDeclaration();
    const item = component.dataSource.data[0];

    component.openVerifyForm(item);
    component.verifyForm.patchValue({
      verifiedAmount: 140000,
      status: 'REJECTED',
      remarks: 'Insufficient proof'
    });

    component.verifyProof(item);
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to verify proof');
  });
});
