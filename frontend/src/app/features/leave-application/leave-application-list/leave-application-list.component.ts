import { Component, OnInit, ViewChild, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
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
import { LeaveApplicationService } from '../../../services/leave-application.service';
import { NotificationService } from '../../../services/notification.service';
import { LeaveApplicationResponse } from '../../../models/leave-application.model';

@Component({
  selector: 'app-leave-application-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
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
  templateUrl: './leave-application-list.component.html',
  styleUrl: './leave-application-list.component.scss',
})
export class LeaveApplicationListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'employeeName', 'leaveTypeName', 'fromDate', 'toDate', 'numberOfDays', 'status', 'actions'
  ];
  dataSource = new MatTableDataSource<LeaveApplicationResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  selectedStatus = '';
  statuses = ['PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private leaveApplicationService: LeaveApplicationService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadRecords();
  }

  loadRecords(): void {
    this.loading = true;

    if (this.selectedStatus) {
      this.leaveApplicationService.getByStatus(this.selectedStatus, this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load leave applications');
            this.loading = false;
          }
        });
    } else {
      this.leaveApplicationService.getAll(this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load leave applications');
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
    this.selectedStatus = '';
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadRecords();
  }

  approveRequest(record: LeaveApplicationResponse): void {
    this.leaveApplicationService.approve(record.id, { status: 'APPROVED', approvedBy: 'admin', remarks: '' })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Leave application approved successfully');
          this.loadRecords();
        },
        error: () => this.notificationService.error('Failed to approve leave application')
      });
  }

  rejectRequest(record: LeaveApplicationResponse): void {
    this.leaveApplicationService.approve(record.id, { status: 'REJECTED', approvedBy: 'admin', remarks: '' })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Leave application rejected successfully');
          this.loadRecords();
        },
        error: () => this.notificationService.error('Failed to reject leave application')
      });
  }

  cancelRequest(record: LeaveApplicationResponse): void {
    this.leaveApplicationService.cancel(record.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Leave application cancelled successfully');
          this.loadRecords();
        },
        error: () => this.notificationService.error('Failed to cancel leave application')
      });
  }

  deleteRequest(record: LeaveApplicationResponse): void {
    if (confirm('Are you sure you want to delete this leave application?')) {
      this.leaveApplicationService.delete(record.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Leave application deleted successfully');
            this.loadRecords();
          },
          error: () => this.notificationService.error('Failed to delete leave application')
        });
    }
  }
}
