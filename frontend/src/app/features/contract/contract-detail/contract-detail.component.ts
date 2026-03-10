import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ContractService } from '../../../services/contract.service';
import { NotificationService } from '../../../services/notification.service';
import { ContractResponse, ContractAmendmentResponse, ContractAmendmentRequest } from '../../../models/contract.model';

@Component({
  selector: 'app-contract-detail',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatListModule,
    MatDividerModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTooltipModule
  ],
  templateUrl: './contract-detail.component.html',
  styleUrl: './contract-detail.component.scss',
})
export class ContractDetailComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  contract: ContractResponse | null = null;
  amendments: ContractAmendmentResponse[] = [];
  loading = true;
  amendmentsLoading = false;
  showAmendmentForm = false;
  savingAmendment = false;

  amendmentForm!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private contractService: ContractService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initAmendmentForm();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadContract(+id);
      this.loadAmendments(+id);
    } else {
      this.router.navigate(['/contracts']);
    }
  }

  private initAmendmentForm(): void {
    this.amendmentForm = this.fb.group({
      amendmentDate: ['', Validators.required],
      description: ['', [Validators.required, Validators.maxLength(500)]],
      oldTerms: [''],
      newTerms: ['', Validators.required]
    });
  }

  loadContract(id: number): void {
    this.loading = true;
    this.contractService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: contract => {
          this.contract = contract;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load contract details');
          this.loading = false;
          this.router.navigate(['/contracts']);
        }
      });
  }

  loadAmendments(id: number): void {
    this.amendmentsLoading = true;
    this.contractService.getAmendments(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: amendments => {
          this.amendments = amendments;
          this.amendmentsLoading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load amendments');
          this.amendmentsLoading = false;
        }
      });
  }

  toggleAmendmentForm(): void {
    this.showAmendmentForm = !this.showAmendmentForm;
    if (!this.showAmendmentForm) {
      this.amendmentForm.reset();
    }
  }

  submitAmendment(): void {
    if (this.amendmentForm.invalid || !this.contract) {
      this.amendmentForm.markAllAsTouched();
      return;
    }

    this.savingAmendment = true;
    const request: ContractAmendmentRequest = {
      contractId: this.contract.id,
      amendmentDate: this.formatDateValue(this.amendmentForm.get('amendmentDate')?.value),
      description: this.amendmentForm.get('description')?.value,
      oldTerms: this.amendmentForm.get('oldTerms')?.value,
      newTerms: this.amendmentForm.get('newTerms')?.value
    };

    this.contractService.addAmendment(this.contract.id, request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Amendment added successfully');
          this.savingAmendment = false;
          this.showAmendmentForm = false;
          this.amendmentForm.reset();
          if (this.contract) {
            this.loadAmendments(this.contract.id);
          }
        },
        error: () => {
          this.notificationService.error('Failed to add amendment');
          this.savingAmendment = false;
        }
      });
  }

  renewContract(): void {
    if (!this.contract) return;

    if (confirm(`Are you sure you want to renew this contract for ${this.contract.employeeName}?`)) {
      this.contractService.renew(this.contract.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: renewed => {
            this.notificationService.success('Contract renewed successfully');
            this.contract = renewed;
          },
          error: () => this.notificationService.error('Failed to renew contract')
        });
    }
  }

  editContract(): void {
    if (this.contract) {
      this.router.navigate(['/contracts', this.contract.id, 'edit']);
    }
  }

  goBack(): void {
    this.router.navigate(['/contracts']);
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'primary';
      case 'EXPIRED': return 'warn';
      case 'RENEWED': return 'accent';
      case 'TERMINATED': return 'warn';
      default: return '';
    }
  }

  formatType(type: string): string {
    return type.replace(/_/g, ' ');
  }

  getTimelineItems(): Array<{ date: string; label: string; type: string }> {
    if (!this.contract) return [];

    const items: Array<{ date: string; label: string; type: string }> = [
      { date: this.contract.startDate, label: 'Contract Start', type: 'start' }
    ];

    for (const amendment of this.amendments) {
      items.push({
        date: amendment.amendmentDate,
        label: amendment.description,
        type: 'amendment'
      });
    }

    items.push({ date: this.contract.endDate, label: 'Contract End', type: 'end' });

    items.sort((a, b) => a.date.localeCompare(b.date));
    return items;
  }

  private formatDateValue(value: unknown): string {
    if (value instanceof Date) {
      const year = value.getFullYear();
      const month = String(value.getMonth() + 1).padStart(2, '0');
      const day = String(value.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    }
    return String(value ?? '');
  }
}
