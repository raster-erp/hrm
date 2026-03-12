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
import { CompOffService } from '../../../services/comp-off.service';
import { NotificationService } from '../../../services/notification.service';
import { CompOffCreditResponse } from '../../../models/comp-off.model';

@Component({
  selector: 'app-comp-off-list',
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
  templateUrl: './comp-off-list.component.html',
  styleUrl: './comp-off-list.component.scss',
})
export class CompOffListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'employeeName', 'workedDate', 'reason', 'creditDate', 'expiryDate', 'hoursWorked', 'status', 'actions'
  ];
  dataSource = new MatTableDataSource<CompOffCreditResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  selectedStatus = '';
  statuses = ['PENDING', 'APPROVED', 'REJECTED', 'USED', 'EXPIRED'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private compOffService: CompOffService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadRecords();
  }

  loadRecords(): void {
    this.loading = true;

    if (this.selectedStatus) {
      this.compOffService.getByStatus(this.selectedStatus, this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load comp-off requests');
            this.loading = false;
          }
        });
    } else {
      this.compOffService.getAll(this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load comp-off requests');
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

  isExpiringSoon(record: CompOffCreditResponse): boolean {
    if (record.status !== 'APPROVED') return false;
    const expiry = new Date(record.expiryDate);
    const now = new Date();
    const diffDays = Math.ceil((expiry.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
    return diffDays <= 7 && diffDays >= 0;
  }
}
