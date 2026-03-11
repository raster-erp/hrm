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
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { WfhRequestService } from '../../../services/wfh-request.service';
import { NotificationService } from '../../../services/notification.service';
import { WfhDashboardResponse } from '../../../models/wfh-request.model';

@Component({
  selector: 'app-wfh-dashboard',
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
    MatChipsModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './wfh-dashboard.component.html',
  styleUrl: './wfh-dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WfhDashboardComponent {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'employeeName', 'employeeCode', 'totalRequests', 'approvedRequests',
    'pendingRequests', 'rejectedRequests', 'checkedInToday'
  ];
  dataSource = new MatTableDataSource<WfhDashboardResponse>();
  loading = false;

  startDate: string;
  endDate: string;

  constructor(
    private wfhRequestService: WfhRequestService,
    private notificationService: NotificationService
  ) {
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    this.startDate = this.formatDate(firstDay);
    this.endDate = this.formatDate(today);
  }

  loadDashboard(): void {
    this.loading = true;
    this.wfhRequestService.getDashboard(this.startDate, this.endDate)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.dataSource.data = data;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load WFH dashboard');
          this.loading = false;
        }
      });
  }

  getTotalRequests(field: keyof WfhDashboardResponse): number {
    return this.dataSource.data.reduce((sum, row) => sum + (row[field] as number), 0);
  }

  getTotalCheckedIn(): number {
    return this.dataSource.data.filter(row => row.checkedInToday).length;
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
