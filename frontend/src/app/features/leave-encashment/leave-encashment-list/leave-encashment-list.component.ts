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
import { LeaveEncashmentService } from '../../../services/leave-encashment.service';
import { NotificationService } from '../../../services/notification.service';
import { LeaveEncashmentResponse } from '../../../models/leave-encashment.model';

@Component({
  selector: 'app-leave-encashment-list',
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
  templateUrl: './leave-encashment-list.component.html',
  styleUrl: './leave-encashment-list.component.scss',
})
export class LeaveEncashmentListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'employeeName', 'leaveTypeName', 'year', 'numberOfDays', 'perDaySalary', 'totalAmount', 'status', 'actions'
  ];
  dataSource = new MatTableDataSource<LeaveEncashmentResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  selectedStatus = '';
  statuses = ['PENDING', 'APPROVED', 'REJECTED', 'PAID'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private leaveEncashmentService: LeaveEncashmentService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadRecords();
  }

  loadRecords(): void {
    this.loading = true;

    if (this.selectedStatus) {
      this.leaveEncashmentService.getByStatus(this.selectedStatus, this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load leave encashment requests');
            this.loading = false;
          }
        });
    } else {
      this.leaveEncashmentService.getAll(this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load leave encashment requests');
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

  markAsPaid(record: LeaveEncashmentResponse): void {
    this.leaveEncashmentService.markAsPaid(record.id, 'admin')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Encashment marked as paid successfully');
          this.loadRecords();
        },
        error: () => this.notificationService.error('Failed to mark encashment as paid')
      });
  }
}
