import { Component, OnInit, ViewChild, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AttendanceDeviationService } from '../../../services/attendance-deviation.service';
import { NotificationService } from '../../../services/notification.service';
import { AttendanceDeviationResponse } from '../../../models/attendance-deviation.model';

@Component({
  selector: 'app-deviation-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule,
    MatCardModule,
    MatChipsModule,
    MatTooltipModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './deviation-list.component.html',
  styleUrl: './deviation-list.component.scss',
})
export class DeviationListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'employeeName', 'deviationDate', 'type', 'deviationMinutes', 'scheduledTime', 'penaltyAction', 'status', 'actions'
  ];
  dataSource = new MatTableDataSource<AttendanceDeviationResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  selectedType = '';
  selectedStatus = '';
  types = ['LATE_COMING', 'EARLY_GOING'];
  statuses = ['PENDING', 'APPROVED', 'WAIVED'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private deviationService: AttendanceDeviationService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadRecords();
  }

  loadRecords(): void {
    this.loading = true;

    if (this.selectedType) {
      this.deviationService.getByType(this.selectedType, this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load attendance deviations');
            this.loading = false;
          }
        });
    } else if (this.selectedStatus) {
      this.deviationService.getByStatus(this.selectedStatus, this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load attendance deviations');
            this.loading = false;
          }
        });
    } else {
      this.deviationService.getAll(this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load attendance deviations');
            this.loading = false;
          }
        });
    }
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadRecords();
  }

  onSearch(): void {
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadRecords();
  }

  clearFilters(): void {
    this.selectedType = '';
    this.selectedStatus = '';
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadRecords();
  }

  formatType(type: string): string {
    return type === 'LATE_COMING' ? 'Late Coming' : 'Early Going';
  }

  approveDeviation(record: AttendanceDeviationResponse): void {
    this.deviationService.approve(record.id, { status: 'APPROVED', approvedBy: 'admin', remarks: '' })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Deviation approved successfully');
          this.loadRecords();
        },
        error: () => this.notificationService.error('Failed to approve deviation')
      });
  }

  waiveDeviation(record: AttendanceDeviationResponse): void {
    this.deviationService.approve(record.id, { status: 'WAIVED', approvedBy: 'admin', remarks: '' })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Deviation waived successfully');
          this.loadRecords();
        },
        error: () => this.notificationService.error('Failed to waive deviation')
      });
  }

  deleteDeviation(record: AttendanceDeviationResponse): void {
    if (confirm('Are you sure you want to delete this attendance deviation?')) {
      this.deviationService.delete(record.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Deviation deleted successfully');
            this.loadRecords();
          },
          error: () => this.notificationService.error('Failed to delete deviation')
        });
    }
  }
}
