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
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { Chart, registerables } from 'chart.js';
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

Chart.register(...registerables);

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
    MatProgressSpinnerModule,
    BaseChartDirective
  ],
  templateUrl: './analytics-dashboard.component.html',
  styleUrl: './analytics-dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AnalyticsDashboardComponent {
  private readonly destroyRef = inject(DestroyRef);

  reportTypes = ['LEAVE_TREND', 'ABSENTEEISM_RATE', 'LEAVE_UTILIZATION'];
  selectedReportType = 'LEAVE_TREND';

  exportFormats = ['CSV', 'EXCEL', 'PDF'];
  genderOptions = ['', 'Male', 'Female', 'Other'];
  ageGroupOptions = [
    { value: '', label: 'All' },
    { value: 'UNDER_25', label: 'Under 25' },
    { value: '25_34', label: '25-34' },
    { value: '35_44', label: '35-44' },
    { value: '45_54', label: '45-54' },
    { value: '55_PLUS', label: '55+' }
  ];

  startYear: number;
  startMonth: number;
  endYear: number;
  endMonth: number;
  year: number;
  startDate: string;
  endDate: string;
  departmentId: number | null = null;
  designationId: number | null = null;
  gender = '';
  ageGroup = '';

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

  trendChartData: ChartData<'line'> = { labels: [], datasets: [] };
  trendChartOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    plugins: { legend: { position: 'top' } },
    scales: { y: { beginAtZero: true, title: { display: true, text: 'Days' } } }
  };

  absenteeismChartData: ChartData<'bar'> = { labels: [], datasets: [] };
  absenteeismChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    plugins: { legend: { position: 'top' } },
    scales: { y: { beginAtZero: true, title: { display: true, text: 'Rate (%)' } } }
  };

  utilizationChartData: ChartData<'pie'> = { labels: [], datasets: [] };
  utilizationChartOptions: ChartConfiguration<'pie'>['options'] = {
    responsive: true,
    plugins: { legend: { position: 'right' } }
  };

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

    const desigId = this.designationId ?? undefined;
    const gen = this.gender || undefined;
    const age = this.ageGroup || undefined;

    switch (this.selectedReportType) {
      case 'LEAVE_TREND':
        this.analyticsService.getLeaveTrend(this.startYear, this.startMonth, this.endYear, this.endMonth,
            this.departmentId ?? undefined, desigId, gen, age)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: (report) => {
              this.trendReport = report;
              this.trendDataSource.data = report.entries;
              this.buildTrendChart(report);
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
            this.departmentId ?? undefined, desigId, gen, age)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: (report) => {
              this.absenteeismReport = report;
              this.absenteeismDataSource.data = report.entries;
              this.buildAbsenteeismChart(report);
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
        this.analyticsService.getLeaveUtilization(this.year, this.departmentId ?? undefined,
            desigId, gen, age)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: (report) => {
              this.utilizationReport = report;
              this.utilizationDataSource.data = report.entries;
              this.buildUtilizationChart(report);
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

  downloadReport(format: string): void {
    const params: Record<string, string> = {};
    if (this.departmentId) {
      params['departmentId'] = String(this.departmentId);
    }
    if (this.designationId) {
      params['designationId'] = String(this.designationId);
    }
    if (this.gender) {
      params['gender'] = this.gender;
    }
    if (this.ageGroup) {
      params['ageGroup'] = this.ageGroup;
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

    const extensions: Record<string, string> = { CSV: 'csv', EXCEL: 'xlsx', PDF: 'pdf' };
    const ext = extensions[format] || 'csv';

    this.analyticsService.exportReport(this.selectedReportType, format, params)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `${this.selectedReportType.toLowerCase()}_report.${ext}`;
          a.click();
          window.URL.revokeObjectURL(url);
          this.notificationService.success('Report downloaded successfully');
        },
        error: () => {
          this.notificationService.error('Failed to download report');
        }
      });
  }

  downloadCsv(): void {
    this.downloadReport('CSV');
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

  buildTrendChart(report: LeaveTrendReport): void {
    const monthLabels: string[] = [];
    const leaveTypeMap = new Map<string, number[]>();

    const monthKeys: string[] = [];
    for (const entry of report.entries) {
      const key = `${entry.year}-${entry.month}`;
      if (!monthKeys.includes(key)) {
        monthKeys.push(key);
        monthLabels.push(`${this.getMonthName(entry.month)} ${entry.year}`);
      }
    }

    for (const entry of report.entries) {
      if (!leaveTypeMap.has(entry.leaveTypeName)) {
        leaveTypeMap.set(entry.leaveTypeName, new Array(monthKeys.length).fill(0));
      }
      const key = `${entry.year}-${entry.month}`;
      const idx = monthKeys.indexOf(key);
      if (idx >= 0) {
        leaveTypeMap.get(entry.leaveTypeName)![idx] = entry.totalDays;
      }
    }

    const colors = ['#1976d2', '#388e3c', '#f57c00', '#d32f2f', '#7b1fa2', '#0097a7'];
    let colorIdx = 0;
    const datasets = Array.from(leaveTypeMap.entries()).map(([name, data]) => ({
      label: name,
      data,
      borderColor: colors[colorIdx % colors.length],
      backgroundColor: colors[colorIdx++ % colors.length] + '33',
      fill: true,
      tension: 0.3
    }));

    this.trendChartData = { labels: monthLabels, datasets };
  }

  buildAbsenteeismChart(report: AbsenteeismRateReport): void {
    this.absenteeismChartData = {
      labels: report.entries.map(e => e.departmentName),
      datasets: [{
        label: 'Absenteeism Rate (%)',
        data: report.entries.map(e => e.absenteeismRate),
        backgroundColor: report.entries.map((_, i) => {
          const colors = ['#1976d2', '#388e3c', '#f57c00', '#d32f2f', '#7b1fa2', '#0097a7'];
          return colors[i % colors.length] + 'cc';
        })
      }]
    };
  }

  buildUtilizationChart(report: LeaveUtilizationReport): void {
    const leaveTypeMap = new Map<string, { used: number; available: number }>();
    for (const entry of report.entries) {
      const existing = leaveTypeMap.get(entry.leaveTypeName) || { used: 0, available: 0 };
      existing.used += entry.used;
      existing.available += entry.available;
      leaveTypeMap.set(entry.leaveTypeName, existing);
    }

    const labels = Array.from(leaveTypeMap.keys());
    const usedData = labels.map(l => leaveTypeMap.get(l)!.used);
    const colors = ['#1976d2', '#388e3c', '#f57c00', '#d32f2f', '#7b1fa2', '#0097a7'];

    this.utilizationChartData = {
      labels,
      datasets: [{
        data: usedData,
        backgroundColor: labels.map((_, i) => colors[i % colors.length] + 'cc')
      }]
    };
  }

  private clearReportData(): void {
    this.trendReport = null;
    this.absenteeismReport = null;
    this.utilizationReport = null;
    this.trendDataSource.data = [];
    this.absenteeismDataSource.data = [];
    this.utilizationDataSource.data = [];
    this.trendChartData = { labels: [], datasets: [] };
    this.absenteeismChartData = { labels: [], datasets: [] };
    this.utilizationChartData = { labels: [], datasets: [] };
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
