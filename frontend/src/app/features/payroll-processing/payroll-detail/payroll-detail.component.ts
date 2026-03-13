import { Component, OnInit, ViewChild, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
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
import { PayrollRunResponse, PayrollDetailResponse } from '../../../models/payroll.model';

interface ComponentBreakupItem {
  name: string;
  type: string;
  amount: number;
}

@Component({
  selector: 'app-payroll-detail',
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
  templateUrl: './payroll-detail.component.html',
  styleUrl: './payroll-detail.component.scss',
})
export class PayrollDetailComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  readonly monthNames = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  runId!: number;
  run: PayrollRunResponse | null = null;
  loading = false;
  detailsLoading = false;

  displayedColumns: string[] = [
    'employeeCode', 'employeeName', 'basicSalary', 'grossSalary',
    'totalDeductions', 'netSalary', 'daysPayable', 'lopDays', 'actions'
  ];
  dataSource = new MatTableDataSource<PayrollDetailResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;

  selectedDetail: PayrollDetailResponse | null = null;
  breakupItems: ComponentBreakupItem[] = [];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private payrollService: PayrollService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.runId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadRun();
    this.loadDetails();
  }

  loadRun(): void {
    this.loading = true;
    this.payrollService.getRunById(this.runId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: run => {
          this.run = run;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load payroll run');
          this.loading = false;
        }
      });
  }

  loadDetails(): void {
    this.detailsLoading = true;
    this.payrollService.getDetails(this.runId, this.pageIndex, this.pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          this.dataSource.data = page.content;
          this.totalElements = page.totalElements;
          this.detailsLoading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load payroll details');
          this.detailsLoading = false;
        }
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadDetails();
  }

  formatPeriod(): string {
    if (!this.run) return '';
    return `${this.monthNames[this.run.periodMonth - 1]} ${this.run.periodYear}`;
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

  hasVariance(detail: PayrollDetailResponse): boolean {
    if (detail.basicSalary === 0) return false;
    const ratio = Math.abs(detail.netSalary - detail.basicSalary) / detail.basicSalary;
    return ratio > 0.5;
  }

  viewBreakup(detail: PayrollDetailResponse): void {
    this.selectedDetail = detail;
    try {
      this.breakupItems = detail.componentBreakup ? JSON.parse(detail.componentBreakup) : [];
    } catch {
      this.breakupItems = [];
    }
  }

  closeBreakup(): void {
    this.selectedDetail = null;
    this.breakupItems = [];
  }

  computePayroll(): void {
    this.payrollService.computePayroll(this.runId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Payroll computation started');
          this.loadRun();
          this.loadDetails();
        },
        error: () => this.notificationService.error('Failed to compute payroll')
      });
  }

  verifyRun(): void {
    this.payrollService.verifyRun(this.runId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Payroll verified successfully');
          this.loadRun();
        },
        error: () => this.notificationService.error('Failed to verify payroll')
      });
  }

  finalizeRun(): void {
    if (confirm('Are you sure you want to finalize this payroll run? This action cannot be undone.')) {
      this.payrollService.finalizeRun(this.runId)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Payroll finalized successfully');
            this.loadRun();
          },
          error: () => this.notificationService.error('Failed to finalize payroll')
        });
    }
  }

  reverseRun(): void {
    if (confirm('Are you sure you want to reverse this payroll run?')) {
      this.payrollService.reverseRun(this.runId)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Payroll reversed successfully');
            this.loadRun();
          },
          error: () => this.notificationService.error('Failed to reverse payroll')
        });
    }
  }

  goBack(): void {
    this.router.navigate(['/payroll-processing']);
  }
}
