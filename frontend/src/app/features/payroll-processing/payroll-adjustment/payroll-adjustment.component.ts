import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PayrollService } from '../../../services/payroll.service';
import { NotificationService } from '../../../services/notification.service';
import { PayrollAdjustmentRequest, PayrollAdjustmentResponse } from '../../../models/payroll.model';

@Component({
  selector: 'app-payroll-adjustment',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatCardModule,
    MatTooltipModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './payroll-adjustment.component.html',
  styleUrl: './payroll-adjustment.component.scss',
})
export class PayrollAdjustmentComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  runId!: number;
  adjustmentForm!: FormGroup;
  saving = false;
  loading = false;

  readonly adjustmentTypes = [
    { value: 'ADDITION', label: 'Addition' },
    { value: 'DEDUCTION', label: 'Deduction' }
  ];

  displayedColumns: string[] = [
    'employeeCode', 'employeeName', 'adjustmentType', 'componentName', 'amount', 'reason', 'actions'
  ];
  dataSource = new MatTableDataSource<PayrollAdjustmentResponse>();

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private payrollService: PayrollService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.runId = Number(this.route.snapshot.paramMap.get('id'));
    this.initForm();
    this.loadAdjustments();
  }

  private initForm(): void {
    this.adjustmentForm = this.fb.group({
      employeeId: [null, [Validators.required, Validators.min(1)]],
      adjustmentType: ['', Validators.required],
      componentName: ['', [Validators.required, Validators.maxLength(100)]],
      amount: [null, [Validators.required, Validators.min(0.01)]],
      reason: ['', [Validators.required, Validators.maxLength(500)]]
    });
  }

  loadAdjustments(): void {
    this.loading = true;
    this.payrollService.getAdjustments(this.runId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: adjustments => {
          this.dataSource.data = adjustments;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load adjustments');
          this.loading = false;
        }
      });
  }

  onSubmit(): void {
    if (this.adjustmentForm.invalid) {
      this.adjustmentForm.markAllAsTouched();
      this.notificationService.error('Please fill in all required fields correctly');
      return;
    }

    this.saving = true;
    const request: PayrollAdjustmentRequest = {
      payrollRunId: this.runId,
      ...this.adjustmentForm.value
    };

    this.payrollService.createAdjustment(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Adjustment added successfully');
          this.saving = false;
          this.adjustmentForm.reset();
          this.loadAdjustments();
        },
        error: () => {
          this.notificationService.error('Failed to add adjustment');
          this.saving = false;
        }
      });
  }

  deleteAdjustment(adjustment: PayrollAdjustmentResponse): void {
    if (confirm(`Are you sure you want to delete this adjustment for "${adjustment.employeeName}"?`)) {
      this.payrollService.deleteAdjustment(adjustment.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Adjustment deleted successfully');
            this.loadAdjustments();
          },
          error: () => this.notificationService.error('Failed to delete adjustment')
        });
    }
  }

  goBack(): void {
    this.router.navigate(['/payroll-processing', this.runId]);
  }
}
