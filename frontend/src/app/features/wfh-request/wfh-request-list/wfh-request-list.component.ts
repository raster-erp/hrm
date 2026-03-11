import { Component, OnInit, ViewChild, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
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
import { WfhRequestService } from '../../../services/wfh-request.service';
import { NotificationService } from '../../../services/notification.service';
import { WfhRequestResponse } from '../../../models/wfh-request.model';

@Component({
  selector: 'app-wfh-request-list',
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
  templateUrl: './wfh-request-list.component.html',
  styleUrl: './wfh-request-list.component.scss',
})
export class WfhRequestListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'employeeName', 'requestDate', 'reason', 'status', 'actions'
  ];
  dataSource = new MatTableDataSource<WfhRequestResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  selectedStatus = '';
  statuses = ['PENDING', 'APPROVED', 'REJECTED'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private wfhRequestService: WfhRequestService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadRecords();
  }

  loadRecords(): void {
    this.loading = true;

    if (this.selectedStatus) {
      this.wfhRequestService.getByStatus(this.selectedStatus, this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load WFH requests');
            this.loading = false;
          }
        });
    } else {
      this.wfhRequestService.getAll(this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load WFH requests');
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

  approveRecord(record: WfhRequestResponse): void {
    this.wfhRequestService.approve(record.id, { status: 'APPROVED', approvedBy: 'admin', remarks: '' })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('WFH request approved successfully');
          this.loadRecords();
        },
        error: () => this.notificationService.error('Failed to approve WFH request')
      });
  }

  rejectRecord(record: WfhRequestResponse): void {
    this.wfhRequestService.approve(record.id, { status: 'REJECTED', approvedBy: 'admin', remarks: '' })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('WFH request rejected successfully');
          this.loadRecords();
        },
        error: () => this.notificationService.error('Failed to reject WFH request')
      });
  }

  editRecord(record: WfhRequestResponse): void {
    this.router.navigate(['/wfh-requests', record.id, 'edit']);
  }

  deleteRecord(record: WfhRequestResponse): void {
    if (confirm(`Are you sure you want to delete this WFH request?`)) {
      this.wfhRequestService.delete(record.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('WFH request deleted successfully');
            this.loadRecords();
          },
          error: () => this.notificationService.error('Failed to delete WFH request')
        });
    }
  }
}
