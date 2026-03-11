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
import { RegularizationRequestService } from '../../../services/regularization-request.service';
import { NotificationService } from '../../../services/notification.service';
import { RegularizationRequestResponse } from '../../../models/regularization-request.model';

@Component({
  selector: 'app-regularization-approval',
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
  templateUrl: './regularization-approval.component.html',
  styleUrl: './regularization-approval.component.scss',
})
export class RegularizationApprovalComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'select', 'employeeName', 'requestDate', 'type', 'reason', 'correctedPunchIn', 'correctedPunchOut', 'actions'
  ];
  dataSource = new MatTableDataSource<RegularizationRequestResponse>();
  selection = new SelectionModel<RegularizationRequestResponse>(true, []);
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

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
    this.selection.clear();

    this.regularizationService.getByStatus('PENDING', this.pageIndex, this.pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          this.dataSource.data = page.content;
          this.totalElements = page.totalElements;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load pending requests');
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

  bulkApprove(): void {
    this.bulkAction('APPROVED');
  }

  bulkReject(): void {
    this.bulkAction('REJECTED');
  }

  private bulkAction(status: string): void {
    const selected = this.selection.selected;
    if (selected.length === 0) {
      this.notificationService.error('No requests selected');
      return;
    }

    const label = status === 'APPROVED' ? 'approved' : 'rejected';
    const requests = selected.map(record =>
      this.regularizationService.approve(record.id, { status, approvedBy: 'admin', remarks: '' })
    );

    forkJoin(requests)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: results => {
          this.notificationService.success(`${results.length} request(s) ${label} successfully`);
          this.loadRecords();
        },
        error: () => this.notificationService.error(`Failed to ${label.slice(0, -1)} one or more requests`)
      });
  }
}
