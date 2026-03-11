import { Component, ChangeDetectionStrategy, ChangeDetectorRef, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
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
import { AttendanceReportService } from '../../../services/attendance-report.service';
import { NotificationService } from '../../../services/notification.service';
import {
  DailyMusterEntry,
  DailyMusterReport,
  MonthlySummaryEntry,
  MonthlySummaryReport,
  AbsenteeEntry,
  AbsenteeListReport,
} from '../../../models/attendance-report.model';

@Component({
  selector: 'app-report-viewer',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
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
  templateUrl: './report-viewer.component.html',
  styleUrl: './report-viewer.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReportViewerComponent {
  private readonly destroyRef = inject(DestroyRef);

  reportTypes = ['DAILY_MUSTER', 'MONTHLY_SUMMARY', 'ABSENTEE_LIST'];
  selectedReportType = 'DAILY_MUSTER';

  date: string;
  year: number;
  month: number;
  startDate: string;
  endDate: string;
  departmentId: number | null = null;

  loading = false;

  dailyMusterReport: DailyMusterReport | null = null;
  monthlySummaryReport: MonthlySummaryReport | null = null;
  absenteeListReport: AbsenteeListReport | null = null;

  dailyMusterColumns = ['employeeName', 'employeeCode', 'departmentName', 'firstPunchIn', 'lastPunchOut', 'totalPunches', 'status'];
  monthlySummaryColumns = ['employeeName', 'employeeCode', 'departmentName', 'totalPresent', 'totalAbsent', 'totalIncomplete', 'totalWorkingDays'];
  absenteeColumns = ['employeeName', 'employeeCode', 'departmentName', 'absentDate'];

  dailyMusterDataSource = new MatTableDataSource<DailyMusterEntry>();
  monthlySummaryDataSource = new MatTableDataSource<MonthlySummaryEntry>();
  absenteeDataSource = new MatTableDataSource<AbsenteeEntry>();

  constructor(
    private reportService: AttendanceReportService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {
    const today = new Date();
    this.date = this.formatDate(today);
    this.year = today.getFullYear();
    this.month = today.getMonth() + 1;
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    this.startDate = this.formatDate(firstDay);
    this.endDate = this.formatDate(today);
  }

  loadReport(): void {
    this.loading = true;
    this.clearReportData();

    switch (this.selectedReportType) {
      case 'DAILY_MUSTER':
        this.reportService.getDailyMuster(this.date, this.departmentId ?? undefined)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: (report) => {
              this.dailyMusterReport = report;
              this.dailyMusterDataSource.data = report.entries;
              this.loading = false;
              this.cdr.markForCheck();
            },
            error: () => {
              this.notificationService.error('Failed to load daily muster report');
              this.loading = false;
              this.cdr.markForCheck();
            }
          });
        break;

      case 'MONTHLY_SUMMARY':
        this.reportService.getMonthlySummary(this.year, this.month, this.departmentId ?? undefined)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: (report) => {
              this.monthlySummaryReport = report;
              this.monthlySummaryDataSource.data = report.entries;
              this.loading = false;
              this.cdr.markForCheck();
            },
            error: () => {
              this.notificationService.error('Failed to load monthly summary report');
              this.loading = false;
              this.cdr.markForCheck();
            }
          });
        break;

      case 'ABSENTEE_LIST':
        this.reportService.getAbsenteeList(this.startDate, this.endDate, this.departmentId ?? undefined)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: (report) => {
              this.absenteeListReport = report;
              this.absenteeDataSource.data = report.entries;
              this.loading = false;
              this.cdr.markForCheck();
            },
            error: () => {
              this.notificationService.error('Failed to load absentee list report');
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
      case 'DAILY_MUSTER':
        params['date'] = this.date;
        break;
      case 'MONTHLY_SUMMARY':
        params['year'] = String(this.year);
        params['month'] = String(this.month);
        break;
      case 'ABSENTEE_LIST':
        params['startDate'] = this.startDate;
        params['endDate'] = this.endDate;
        break;
    }

    this.reportService.exportReport(this.selectedReportType, 'CSV', params)
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
      case 'DAILY_MUSTER': return 'Daily Muster';
      case 'MONTHLY_SUMMARY': return 'Monthly Summary';
      case 'ABSENTEE_LIST': return 'Absentee List';
      default: return type;
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'PRESENT': return 'status-present';
      case 'ABSENT': return 'status-absent';
      case 'INCOMPLETE': return 'status-incomplete';
      default: return '';
    }
  }

  private clearReportData(): void {
    this.dailyMusterReport = null;
    this.monthlySummaryReport = null;
    this.absenteeListReport = null;
    this.dailyMusterDataSource.data = [];
    this.monthlySummaryDataSource.data = [];
    this.absenteeDataSource.data = [];
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
