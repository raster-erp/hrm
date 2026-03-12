import { Component, ChangeDetectionStrategy, ChangeDetectorRef, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LeaveAnalyticsService } from '../../../services/leave-analytics.service';
import { NotificationService } from '../../../services/notification.service';
import {
  LeaveTrendEntry,
  LeaveTrendReport,
  AbsenteeismRateEntry,
  AbsenteeismRateReport,
  LeaveUtilizationEntry,
  LeaveUtilizationReport,
} from '../../../models/leave-analytics.model';

@Component({
  selector: 'app-analytics-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCardModule,
    MatChipsModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './analytics-dashboard.component.html',
  styleUrl: './analytics-dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AnalyticsDashboardComponent {
  private readonly destroyRef = inject(DestroyRef);

  reportTypes = ['LEAVE_TREND', 'ABSENTEEISM_RATE', 'LEAVE_UTILIZATION'];
  selectedReportType = 'LEAVE_TREND';

  startYear: number;
  startMonth: number;
  endYear: number;
  endMonth: number;
  year: number;
  startDate: string;
  endDate: string;
  departmentId: number | null = null;

  loading = false;

  trendReport: LeaveTrendReport | null = null;
  absenteeismReport: AbsenteeismRateReport | null = null;
  utilizationReport: LeaveUtilizationReport | null = null;

  trendColumns = ['year', 'month', 'leaveTypeName', 'applicationCount', 'totalDays'];
  absenteeismColumns = ['departmentName', 'employeeCount', 'totalLeaveDays', 'totalWorkingDays', 'absenteeismRate'];
  utilizationColumns = ['employeeName', 'employeeCode', 'departmentName', 'leaveTypeName', 'entitled', 'used', 'available', 'utilizationPercent'];

  trendDataSource = new MatTableDataSource<LeaveTrendEntry>();
  absenteeismDataSource = new MatTableDataSource<AbsenteeismRateEntry>();
  utilizationDataSource = new MatTableDataSource<LeaveUtilizationEntry>();

  constructor(
    private analyticsService: LeaveAnalyticsService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {
    const today = new Date();
    this.year = today.getFullYear();
    this.startYear = today.getFullYear();
    this.startMonth = 1;
    this.endYear = today.getFullYear();
    this.endMonth = today.getMonth() + 1;
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    this.startDate = this.formatDate(firstDay);
    this.endDate = this.formatDate(today);
  }

  loadReport(): void {
    this.loading = true;
    this.clearReportData();

    switch (this.selectedReportType) {
      case 'LEAVE_TREND':
        this.analyticsService.getLeaveTrend(this.startYear, this.startMonth, this.endYear, this.endMonth,
            this.departmentId ?? undefined)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: (report) => {
              this.trendReport = report;
              this.trendDataSource.data = report.entries;
              this.loading = false;
              this.cdr.markForCheck();
            },
            error: () => {
              this.notificationService.error('Failed to load leave trend report');
              this.loading = false;
              this.cdr.markForCheck();
            }
          });
        break;

      case 'ABSENTEEISM_RATE':
        this.analyticsService.getAbsenteeismRate(this.startDate, this.endDate,
            this.departmentId ?? undefined)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: (report) => {
              this.absenteeismReport = report;
              this.absenteeismDataSource.data = report.entries;
              this.loading = false;
              this.cdr.markForCheck();
            },
            error: () => {
              this.notificationService.error('Failed to load absenteeism rate report');
              this.loading = false;
              this.cdr.markForCheck();
            }
          });
        break;

      case 'LEAVE_UTILIZATION':
        this.analyticsService.getLeaveUtilization(this.year, this.departmentId ?? undefined)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: (report) => {
              this.utilizationReport = report;
              this.utilizationDataSource.data = report.entries;
              this.loading = false;
              this.cdr.markForCheck();
            },
            error: () => {
              this.notificationService.error('Failed to load leave utilization report');
              this.loading = false;
              this.cdr.markForCheck();
            }
          });
        break;
    }
  }

  downloadCsv(): void {
    const params: Record<string, string> = {};
    if (this.departmentId) {
      params['departmentId'] = String(this.departmentId);
    }

    switch (this.selectedReportType) {
      case 'LEAVE_TREND':
        params['startYear'] = String(this.startYear);
        params['startMonth'] = String(this.startMonth);
        params['endYear'] = String(this.endYear);
        params['endMonth'] = String(this.endMonth);
        break;
      case 'ABSENTEEISM_RATE':
        params['startDate'] = this.startDate;
        params['endDate'] = this.endDate;
        break;
      case 'LEAVE_UTILIZATION':
        params['year'] = String(this.year);
        break;
    }

    this.analyticsService.exportReport(this.selectedReportType, 'CSV', params)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `${this.selectedReportType.toLowerCase()}_report.csv`;
          a.click();
          window.URL.revokeObjectURL(url);
          this.notificationService.success('Report downloaded successfully');
        },
        error: () => {
          this.notificationService.error('Failed to download report');
        }
      });
  }

  getReportLabel(type: string): string {
    switch (type) {
      case 'LEAVE_TREND': return 'Leave Trend';
      case 'ABSENTEEISM_RATE': return 'Absenteeism Rate';
      case 'LEAVE_UTILIZATION': return 'Leave Utilization';
      default: return type;
    }
  }

  getMonthName(month: number): string {
    const months = ['', 'January', 'February', 'March', 'April', 'May', 'June',
                     'July', 'August', 'September', 'October', 'November', 'December'];
    return months[month] || '';
  }

  private clearReportData(): void {
    this.trendReport = null;
    this.absenteeismReport = null;
    this.utilizationReport = null;
    this.trendDataSource.data = [];
    this.absenteeismDataSource.data = [];
    this.utilizationDataSource.data = [];
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
