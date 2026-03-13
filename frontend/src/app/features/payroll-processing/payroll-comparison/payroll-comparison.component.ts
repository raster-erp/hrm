import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { forkJoin } from 'rxjs';
import { PayrollService } from '../../../services/payroll.service';
import { NotificationService } from '../../../services/notification.service';
import { PayrollRunResponse, PayrollDetailResponse } from '../../../models/payroll.model';

export interface ComparisonRow {
  employeeCode: string;
  employeeName: string;
  currentGross: number;
  previousGross: number;
  grossVariance: number;
  currentNet: number;
  previousNet: number;
  netVariance: number;
}

@Component({
  selector: 'app-payroll-comparison',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './payroll-comparison.component.html',
  styleUrl: './payroll-comparison.component.scss',
})
export class PayrollComparisonComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  readonly monthNames = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  runId!: number;
  currentRun: PayrollRunResponse | null = null;
  previousRun: PayrollRunResponse | null = null;
  loading = false;
  noPreviousRun = false;

  displayedColumns: string[] = [
    'employeeCode', 'employeeName',
    'currentGross', 'previousGross', 'grossVariance',
    'currentNet', 'previousNet', 'netVariance'
  ];
  dataSource = new MatTableDataSource<ComparisonRow>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private payrollService: PayrollService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.runId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadComparison();
  }

  loadComparison(): void {
    this.loading = true;
    this.payrollService.getRunById(this.runId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: run => {
          this.currentRun = run;
          this.findPreviousRun(run);
        },
        error: () => {
          this.notificationService.error('Failed to load payroll run');
          this.loading = false;
        }
      });
  }

  findPreviousRun(currentRun: PayrollRunResponse): void {
    this.payrollService.getAllRuns(0, 1000)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          const prevMonth = currentRun.periodMonth === 1 ? 12 : currentRun.periodMonth - 1;
          const prevYear = currentRun.periodMonth === 1 ? currentRun.periodYear - 1 : currentRun.periodYear;

          const previous = page.content.find(
            r => r.periodYear === prevYear && r.periodMonth === prevMonth
          );

          if (previous) {
            this.previousRun = previous;
            this.loadDetails(currentRun.id, previous.id);
          } else {
            this.noPreviousRun = true;
            this.loading = false;
          }
        },
        error: () => {
          this.notificationService.error('Failed to load payroll runs');
          this.loading = false;
        }
      });
  }

  loadDetails(currentRunId: number, previousRunId: number): void {
    forkJoin({
      current: this.payrollService.getDetails(currentRunId, 0, 1000),
      previous: this.payrollService.getDetails(previousRunId, 0, 1000)
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ current, previous }) => {
          this.buildComparisonData(current.content, previous.content);
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load payroll details');
          this.loading = false;
        }
      });
  }

  buildComparisonData(currentDetails: PayrollDetailResponse[], previousDetails: PayrollDetailResponse[]): void {
    const previousMap = new Map<number, PayrollDetailResponse>();
    for (const detail of previousDetails) {
      previousMap.set(detail.employeeId, detail);
    }

    const rows: ComparisonRow[] = currentDetails.map(current => {
      const prev = previousMap.get(current.employeeId);
      return {
        employeeCode: current.employeeCode,
        employeeName: current.employeeName,
        currentGross: current.grossSalary,
        previousGross: prev ? prev.grossSalary : 0,
        grossVariance: current.grossSalary - (prev ? prev.grossSalary : 0),
        currentNet: current.netSalary,
        previousNet: prev ? prev.netSalary : 0,
        netVariance: current.netSalary - (prev ? prev.netSalary : 0)
      };
    });

    this.dataSource.data = rows;
  }

  formatPeriod(run: PayrollRunResponse | null): string {
    if (!run) return '';
    return `${this.monthNames[run.periodMonth - 1]} ${run.periodYear}`;
  }

  getVarianceClass(value: number): string {
    if (value > 0) return 'variance-positive';
    if (value < 0) return 'variance-negative';
    return '';
  }

  goBack(): void {
    this.router.navigate(['/payroll-processing']);
  }
}
