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
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LeaveBalanceService } from '../../../services/leave-balance.service';
import { NotificationService } from '../../../services/notification.service';
import { LeaveTransactionResponse } from '../../../models/leave-balance.model';

@Component({
  selector: 'app-leave-transaction-history',
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
    MatInputModule,
    MatSelectModule,
    MatCardModule,
    MatChipsModule,
    MatTooltipModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './leave-transaction-history.component.html',
  styleUrl: './leave-transaction-history.component.scss',
})
export class LeaveTransactionHistoryComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'createdAt', 'leaveTypeName', 'transactionType', 'amount', 'balanceAfter', 'description', 'createdBy'
  ];
  dataSource = new MatTableDataSource<LeaveTransactionResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  selectedEmployeeId: number | null = null;
  selectedTransactionType = '';
  transactionTypes = ['CREDIT', 'DEBIT', 'CARRY_FORWARD', 'LAPSE', 'ADJUSTMENT', 'PENDING_DEBIT', 'PENDING_REVERSAL'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private leaveBalanceService: LeaveBalanceService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {}

  loadRecords(): void {
    if (!this.selectedEmployeeId) {
      return;
    }
    this.loading = true;

    this.leaveBalanceService.getTransactions(
      this.selectedEmployeeId,
      this.pageIndex,
      this.pageSize,
      undefined,
      this.selectedTransactionType || undefined
    )
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          this.dataSource.data = page.content;
          this.totalElements = page.totalElements;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load transactions');
          this.loading = false;
        }
      });
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
    this.selectedTransactionType = '';
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadRecords();
  }

  getAmountClass(amount: number): string {
    return amount >= 0 ? 'positive' : 'negative';
  }
}
