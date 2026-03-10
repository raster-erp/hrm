import { Component, ChangeDetectionStrategy, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { OvertimeRecordService } from '../../../services/overtime-record.service';
import { NotificationService } from '../../../services/notification.service';
import { OvertimeSummaryResponse } from '../../../models/overtime-record.model';

@Component({
  selector: 'app-overtime-summary',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatCardModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './overtime-summary.component.html',
  styleUrl: './overtime-summary.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OvertimeSummaryComponent {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'employeeName', 'employeeCode', 'totalOvertimeMinutes', 'approvedOvertimeMinutes',
    'pendingOvertimeMinutes', 'rejectedOvertimeMinutes', 'weightedOvertimeMinutes', 'recordCount'
  ];
  dataSource = new MatTableDataSource<OvertimeSummaryResponse>();
  loading = false;

  startDate: string;
  endDate: string;

  constructor(
    private overtimeRecordService: OvertimeRecordService,
    private notificationService: NotificationService
  ) {
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    this.startDate = this.formatDate(firstDay);
    this.endDate = this.formatDate(today);
  }

  loadSummary(): void {
    this.loading = true;
    this.overtimeRecordService.getSummary(this.startDate, this.endDate)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.dataSource.data = data;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load overtime summary');
          this.loading = false;
        }
      });
  }

  formatMinutes(minutes: number): string {
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    return `${h}h ${m}m`;
  }

  getTotalMinutes(field: keyof OvertimeSummaryResponse): number {
    return this.dataSource.data.reduce((sum, row) => sum + (row[field] as number), 0);
  }

  getTotalRecordCount(): number {
    return this.dataSource.data.reduce((sum, row) => sum + row.recordCount, 0);
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
