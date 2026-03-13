import { Component, OnInit, ViewChild, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PayrollService } from '../../../services/payroll.service';
import { NotificationService } from '../../../services/notification.service';
import { PayrollRunResponse } from '../../../models/payroll.model';

@Component({
  selector: 'app-payroll-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatTooltipModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './payroll-list.component.html',
  styleUrl: './payroll-list.component.scss',
})
export class PayrollListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  readonly monthNames = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  displayedColumns: string[] = [
    'period', 'runDate', 'status', 'employeeCount', 'totalGross', 'totalDeductions', 'totalNet', 'actions'
  ];
  dataSource = new MatTableDataSource<PayrollRunResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private payrollService: PayrollService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadRuns();
  }

  loadRuns(): void {
    this.loading = true;
    this.payrollService.getAllRuns(this.pageIndex, this.pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          this.dataSource.data = page.content;
          this.totalElements = page.totalElements;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load payroll runs');
          this.loading = false;
        }
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadRuns();
  }

  formatPeriod(run: PayrollRunResponse): string {
    return `${this.monthNames[run.periodMonth - 1]} ${run.periodYear}`;
  }

  getStatusClass(status: string): string {
    const statusMap: Record<string, string> = {
      DRAFT: 'status-draft',
      PROCESSING: 'status-processing',
      COMPUTED: 'status-computed',
      VERIFIED: 'status-verified',
      FINALIZED: 'status-finalized',
      REVERSED: 'status-reversed'
    };
    return statusMap[status] || 'status-draft';
  }

  viewDetails(run: PayrollRunResponse): void {
    this.router.navigate(['/payroll-processing', run.id]);
  }

  computePayroll(run: PayrollRunResponse): void {
    this.payrollService.computePayroll(run.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Payroll computation started');
          this.loadRuns();
        },
        error: () => this.notificationService.error('Failed to compute payroll')
      });
  }

  verifyRun(run: PayrollRunResponse): void {
    this.payrollService.verifyRun(run.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Payroll verified successfully');
          this.loadRuns();
        },
        error: () => this.notificationService.error('Failed to verify payroll')
      });
  }

  finalizeRun(run: PayrollRunResponse): void {
    if (confirm('Are you sure you want to finalize this payroll run? This action cannot be undone.')) {
      this.payrollService.finalizeRun(run.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Payroll finalized successfully');
            this.loadRuns();
          },
          error: () => this.notificationService.error('Failed to finalize payroll')
        });
    }
  }

  reverseRun(run: PayrollRunResponse): void {
    if (confirm('Are you sure you want to reverse this payroll run?')) {
      this.payrollService.reverseRun(run.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Payroll reversed successfully');
            this.loadRuns();
          },
          error: () => this.notificationService.error('Failed to reverse payroll')
        });
    }
  }
}
