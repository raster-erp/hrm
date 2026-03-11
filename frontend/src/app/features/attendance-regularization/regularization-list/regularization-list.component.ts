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
import { RegularizationRequestService } from '../../../services/regularization-request.service';
import { NotificationService } from '../../../services/notification.service';
import { RegularizationRequestResponse } from '../../../models/regularization-request.model';

@Component({
  selector: 'app-regularization-list',
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
  templateUrl: './regularization-list.component.html',
  styleUrl: './regularization-list.component.scss',
})
export class RegularizationListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'employeeName', 'requestDate', 'type', 'reason', 'correctedPunchIn', 'correctedPunchOut', 'status', 'actions'
  ];
  dataSource = new MatTableDataSource<RegularizationRequestResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  selectedType = '';
  selectedStatus = '';
  types = ['MISSED_PUNCH', 'ON_DUTY', 'CLIENT_VISIT'];
  statuses = ['PENDING', 'APPROVED', 'REJECTED'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private regularizationService: RegularizationRequestService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadRecords();
  }

  loadRecords(): void {
    this.loading = true;

    if (this.selectedType) {
      this.regularizationService.getByType(this.selectedType, this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load regularization requests');
            this.loading = false;
          }
        });
    } else if (this.selectedStatus) {
      this.regularizationService.getByStatus(this.selectedStatus, this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load regularization requests');
            this.loading = false;
          }
        });
    } else {
      this.regularizationService.getAll(this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load regularization requests');
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
    switch (type) {
      case 'MISSED_PUNCH': return 'Missed Punch';
      case 'ON_DUTY': return 'On Duty';
      case 'CLIENT_VISIT': return 'Client Visit';
      default: return type;
    }
  }

  approveRequest(record: RegularizationRequestResponse): void {
    this.regularizationService.approve(record.id, { status: 'APPROVED', approvedBy: 'admin', remarks: '' })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Request approved successfully');
          this.loadRecords();
        },
        error: () => this.notificationService.error('Failed to approve request')
      });
  }

  rejectRequest(record: RegularizationRequestResponse): void {
    this.regularizationService.approve(record.id, { status: 'REJECTED', approvedBy: 'admin', remarks: '' })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Request rejected successfully');
          this.loadRecords();
        },
        error: () => this.notificationService.error('Failed to reject request')
      });
  }

  deleteRequest(record: RegularizationRequestResponse): void {
    if (confirm('Are you sure you want to delete this regularization request?')) {
      this.regularizationService.delete(record.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Request deleted successfully');
            this.loadRecords();
          },
          error: () => this.notificationService.error('Failed to delete request')
        });
    }
  }
}
