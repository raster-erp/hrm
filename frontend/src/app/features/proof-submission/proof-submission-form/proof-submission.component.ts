import { Component, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { InvestmentDeclarationService } from '../../../services/investment-declaration.service';
import { NotificationService } from '../../../services/notification.service';
import {
  InvestmentDeclarationResponse,
  InvestmentDeclarationItemResponse,
  ProofSubmissionRequest,
  ProofVerificationRequest
} from '../../../models/investment-declaration.model';

@Component({
  selector: 'app-proof-submission',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatChipsModule
  ],
  templateUrl: './proof-submission.component.html',
  styleUrl: './proof-submission.component.scss',
})
export class ProofSubmissionComponent {
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);

  displayedColumns: string[] = [
    'section', 'description', 'declaredAmount', 'proofStatus', 'proofDocumentName', 'actions'
  ];
  dataSource = new MatTableDataSource<InvestmentDeclarationItemResponse>();
  declaration: InvestmentDeclarationResponse | null = null;
  loading = false;
  searchId = '';

  activeProofItemId: number | null = null;
  activeVerifyItemId: number | null = null;

  proofForm: FormGroup = this.fb.group({
    proofDocumentName: ['', Validators.required],
    declaredAmount: [null, [Validators.required, Validators.min(0)]]
  });

  verifyForm: FormGroup = this.fb.group({
    verifiedAmount: [null, [Validators.required, Validators.min(0)]],
    status: ['', Validators.required],
    remarks: ['', Validators.required]
  });

  verificationStatuses = ['VERIFIED', 'REJECTED'];

  constructor(
    private investmentDeclarationService: InvestmentDeclarationService,
    private notificationService: NotificationService
  ) {}

  loadDeclaration(): void {
    const id = Number(this.searchId);
    if (!this.searchId || isNaN(id)) {
      this.notificationService.error('Please enter a valid Declaration ID');
      return;
    }

    this.loading = true;
    this.activeProofItemId = null;
    this.activeVerifyItemId = null;

    this.investmentDeclarationService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (declaration) => {
          this.declaration = declaration;
          this.dataSource.data = declaration.items;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load declaration');
          this.declaration = null;
          this.dataSource.data = [];
          this.loading = false;
        }
      });
  }

  canSubmitProof(item: InvestmentDeclarationItemResponse): boolean {
    return item.proofStatus !== 'SUBMITTED' && item.proofStatus !== 'VERIFIED';
  }

  canVerifyProof(item: InvestmentDeclarationItemResponse): boolean {
    return item.proofStatus === 'SUBMITTED';
  }

  openProofForm(item: InvestmentDeclarationItemResponse): void {
    this.activeProofItemId = item.id;
    this.activeVerifyItemId = null;
    this.proofForm.reset({
      proofDocumentName: item.proofDocumentName || '',
      declaredAmount: item.declaredAmount
    });
  }

  cancelProofForm(): void {
    this.activeProofItemId = null;
    this.proofForm.reset();
  }

  submitProof(item: InvestmentDeclarationItemResponse): void {
    if (this.proofForm.invalid) {
      this.proofForm.markAllAsTouched();
      return;
    }

    const request: ProofSubmissionRequest = {
      itemId: item.id,
      proofDocumentName: this.proofForm.value.proofDocumentName,
      declaredAmount: this.proofForm.value.declaredAmount
    };

    this.investmentDeclarationService.submitProof(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Proof submitted successfully');
          this.activeProofItemId = null;
          this.proofForm.reset();
          this.loadDeclaration();
        },
        error: () => {
          this.notificationService.error('Failed to submit proof');
        }
      });
  }

  openVerifyForm(item: InvestmentDeclarationItemResponse): void {
    this.activeVerifyItemId = item.id;
    this.activeProofItemId = null;
    this.verifyForm.reset({
      verifiedAmount: item.declaredAmount,
      status: '',
      remarks: ''
    });
  }

  cancelVerifyForm(): void {
    this.activeVerifyItemId = null;
    this.verifyForm.reset();
  }

  verifyProof(item: InvestmentDeclarationItemResponse): void {
    if (this.verifyForm.invalid) {
      this.verifyForm.markAllAsTouched();
      return;
    }

    const request: ProofVerificationRequest = {
      itemId: item.id,
      verifiedAmount: this.verifyForm.value.verifiedAmount,
      status: this.verifyForm.value.status,
      remarks: this.verifyForm.value.remarks
    };

    this.investmentDeclarationService.verifyProof(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Proof verified successfully');
          this.activeVerifyItemId = null;
          this.verifyForm.reset();
          this.loadDeclaration();
        },
        error: () => {
          this.notificationService.error('Failed to verify proof');
        }
      });
  }
}
