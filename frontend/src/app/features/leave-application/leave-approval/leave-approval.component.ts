import { Component, OnInit, ViewChild, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin } from 'rxjs';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { SelectionModel } from '@angular/cdk/collections';
import { LeaveApplicationService } from '../../../services/leave-application.service';
import { NotificationService } from '../../../services/notification.service';
import { LeaveApplicationResponse } from '../../../models/leave-application.model';

@Component({
  selector: 'app-leave-approval',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatCheckboxModule,
    MatTooltipModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './leave-approval.component.html',
  styleUrl: './leave-approval.component.scss',
})
export class LeaveApprovalComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'select', 'employeeName', 'leaveTypeName', 'fromDate', 'toDate', 'numberOfDays', 'reason', 'actions'
  ];
  dataSource = new MatTableDataSource<LeaveApplicationResponse>();
  selection = new SelectionModel<LeaveApplicationResponse>(true, []);
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

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
    this.selection.clear();

    this.leaveApplicationService.getByStatus('PENDING', this.pageIndex, this.pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          this.dataSource.data = page.content;
          this.totalElements = page.totalElements;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load pending leave applications');
          this.loading = false;
        }
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadRecords();
  }

  isAllSelected(): boolean {
    return this.selection.selected.length === this.dataSource.data.length && this.dataSource.data.length > 0;
  }

  toggleAllRows(): void {
    if (this.isAllSelected()) {
      this.selection.clear();
    } else {
      this.selection.select(...this.dataSource.data);
    }
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

  bulkApprove(): void {
    this.bulkAction('APPROVED');
  }

  bulkReject(): void {
    this.bulkAction('REJECTED');
  }

  private bulkAction(status: string): void {
    const selected = this.selection.selected;
    if (selected.length === 0) {
      this.notificationService.error('No applications selected');
      return;
    }

    const label = status === 'APPROVED' ? 'approved' : 'rejected';
    const requests = selected.map(record =>
      this.leaveApplicationService.approve(record.id, { status, approvedBy: 'admin', remarks: '' })
    );

    forkJoin(requests)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: results => {
          this.notificationService.success(`${results.length} application(s) ${label} successfully`);
          this.loadRecords();
        },
        error: () => this.notificationService.error(`Failed to ${label.slice(0, -1)} one or more applications`)
      });
  }
}
