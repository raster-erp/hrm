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
import { OvertimeRecordService } from '../../../services/overtime-record.service';
import { NotificationService } from '../../../services/notification.service';
import { OvertimeRecordResponse } from '../../../models/overtime-record.model';

@Component({
  selector: 'app-overtime-record-list',
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
  templateUrl: './overtime-record-list.component.html',
  styleUrl: './overtime-record-list.component.scss',
})
export class OvertimeRecordListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'employeeName', 'overtimeDate', 'overtimePolicyName', 'overtimeMinutes', 'status', 'source', 'actions'
  ];
  dataSource = new MatTableDataSource<OvertimeRecordResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  selectedStatus = '';
  statuses = ['PENDING', 'APPROVED', 'REJECTED'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private overtimeRecordService: OvertimeRecordService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadRecords();
  }

  loadRecords(): void {
    this.loading = true;

    if (this.selectedStatus) {
      this.overtimeRecordService.getByStatus(this.selectedStatus, this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load overtime records');
            this.loading = false;
          }
        });
    } else {
      this.overtimeRecordService.getAll(this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load overtime records');
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

  approveRecord(record: OvertimeRecordResponse): void {
    this.overtimeRecordService.approve(record.id, { status: 'APPROVED', approvedBy: 'admin', remarks: '' })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Overtime record approved successfully');
          this.loadRecords();
        },
        error: () => this.notificationService.error('Failed to approve overtime record')
      });
  }

  rejectRecord(record: OvertimeRecordResponse): void {
    this.overtimeRecordService.approve(record.id, { status: 'REJECTED', approvedBy: 'admin', remarks: '' })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Overtime record rejected successfully');
          this.loadRecords();
        },
        error: () => this.notificationService.error('Failed to reject overtime record')
      });
  }

  editRecord(record: OvertimeRecordResponse): void {
    this.router.navigate(['/overtime-records', record.id, 'edit']);
  }

  deleteRecord(record: OvertimeRecordResponse): void {
    if (confirm(`Are you sure you want to delete this overtime record?`)) {
      this.overtimeRecordService.delete(record.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Overtime record deleted successfully');
            this.loadRecords();
          },
          error: () => this.notificationService.error('Failed to delete overtime record')
        });
    }
  }
}
