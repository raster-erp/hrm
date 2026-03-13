import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TaxComputationService } from '../../../services/tax-computation.service';
import { NotificationService } from '../../../services/notification.service';
import { TaxComputationResponse, Form16DataResponse } from '../../../models/tax-computation.model';

@Component({
  selector: 'app-tax-computation-summary',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatTooltipModule
  ],
  templateUrl: './tax-computation-summary.component.html',
  styleUrl: './tax-computation-summary.component.scss',
})
export class TaxComputationSummaryComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'month', 'grossAnnualIncome', 'totalExemptions', 'taxableIncome',
    'monthlyTds', 'tdsDeductedTillDate', 'remainingTds', 'regime'
  ];
  dataSource = new MatTableDataSource<TaxComputationResponse>();
  loading = false;
  computeLoading = false;
  form16Loading = false;

  searchForm!: FormGroup;
  computeForm!: FormGroup;
  showComputeSection = false;

  form16Data: Form16DataResponse | null = null;

  private readonly monthNames: Record<number, string> = {
    1: 'January', 2: 'February', 3: 'March',
    4: 'April', 5: 'May', 6: 'June',
    7: 'July', 8: 'August', 9: 'September',
    10: 'October', 11: 'November', 12: 'December'
  };

  constructor(
    private fb: FormBuilder,
    private taxComputationService: TaxComputationService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.searchForm = this.fb.group({
      employeeId: [null, [Validators.required, Validators.min(1)]],
      financialYear: ['', [Validators.required]]
    });

    this.computeForm = this.fb.group({
      employeeId: [null, [Validators.required, Validators.min(1)]],
      financialYear: ['', [Validators.required]],
      month: [null, [Validators.required, Validators.min(1), Validators.max(12)]]
    });
  }

  getMonthName(month: number): string {
    return this.monthNames[month] || 'Unknown';
  }

  onSearch(): void {
    if (this.searchForm.invalid) {
      this.notificationService.error('Please enter Employee ID and Financial Year');
      return;
    }

    const { employeeId, financialYear } = this.searchForm.value;
    this.loading = true;
    this.form16Data = null;

    this.taxComputationService.getByEmployeeAndYear(employeeId, financialYear)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (computations) => {
          this.dataSource.data = computations;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load tax computations');
          this.loading = false;
        }
      });
  }

  toggleComputeSection(): void {
    this.showComputeSection = !this.showComputeSection;
  }

  onCompute(): void {
    if (this.computeForm.invalid) {
      this.notificationService.error('Please fill all compute fields');
      return;
    }

    const request = this.computeForm.value;
    this.computeLoading = true;

    this.taxComputationService.computeMonthlyTds(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('TDS computed successfully');
          this.computeLoading = false;
          if (this.searchForm.valid) {
            this.onSearch();
          }
        },
        error: () => {
          this.notificationService.error('Failed to compute TDS');
          this.computeLoading = false;
        }
      });
  }

  onGenerateForm16(): void {
    if (this.searchForm.invalid) {
      this.notificationService.error('Please enter Employee ID and Financial Year');
      return;
    }

    const { employeeId, financialYear } = this.searchForm.value;
    this.form16Loading = true;

    this.taxComputationService.generateForm16Data(employeeId, financialYear)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.form16Data = data;
          this.form16Loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to generate Form 16 data');
          this.form16Loading = false;
        }
      });
  }
}
