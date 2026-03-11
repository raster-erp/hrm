import { Component, ChangeDetectionStrategy, ChangeDetectorRef, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AttendanceReportService } from '../../../services/attendance-report.service';
import { NotificationService } from '../../../services/notification.service';
import { ReportScheduleResponse } from '../../../models/attendance-report.model';

@Component({
  selector: 'app-report-schedule-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatTooltipModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './report-schedule-list.component.html',
  styleUrl: './report-schedule-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReportScheduleListComponent {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'reportName', 'reportType', 'frequency', 'departmentName',
    'exportFormat', 'active', 'lastRunAt', 'actions'
  ];
  dataSource = new MatTableDataSource<ReportScheduleResponse>();
  loading = false;
  pageSize = 10;
  pageIndex = 0;
  totalElements = 0;

  constructor(
    private reportService: AttendanceReportService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {
    this.loadSchedules();
  }

  loadSchedules(): void {
    this.loading = true;
    this.reportService.getAllSchedules(this.pageIndex, this.pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (page) => {
          this.dataSource.data = page.content;
          this.totalElements = page.totalElements;
          this.loading = false;
          this.cdr.markForCheck();
        },
        error: () => {
          this.notificationService.error('Failed to load report schedules');
          this.loading = false;
          this.cdr.markForCheck();
        }
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadSchedules();
  }

  toggleActive(schedule: ReportScheduleResponse): void {
    this.reportService.toggleScheduleActive(schedule.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Schedule status updated');
          this.loadSchedules();
        },
        error: () => {
          this.notificationService.error('Failed to update schedule status');
        }
      });
  }

  deleteSchedule(schedule: ReportScheduleResponse): void {
    if (confirm(`Delete schedule "${schedule.reportName}"?`)) {
      this.reportService.deleteSchedule(schedule.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Schedule deleted');
            this.loadSchedules();
          },
          error: () => {
            this.notificationService.error('Failed to delete schedule');
          }
        });
    }
  }

  getReportLabel(type: string): string {
    switch (type) {
      case 'DAILY_MUSTER': return 'Daily Muster';
      case 'MONTHLY_SUMMARY': return 'Monthly Summary';
      case 'ABSENTEE_LIST': return 'Absentee List';
      default: return type;
    }
  }
}
