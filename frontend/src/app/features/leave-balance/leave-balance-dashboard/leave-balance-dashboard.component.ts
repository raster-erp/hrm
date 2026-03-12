import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { LeaveBalanceService } from '../../../services/leave-balance.service';
import { EmployeeService } from '../../../services/employee.service';
import { NotificationService } from '../../../services/notification.service';
import { LeaveBalanceResponse } from '../../../models/leave-balance.model';

@Component({
  selector: 'app-leave-balance-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatTooltipModule
  ],
  templateUrl: './leave-balance-dashboard.component.html',
  styleUrl: './leave-balance-dashboard.component.scss',
})
export class LeaveBalanceDashboardComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  balances: LeaveBalanceResponse[] = [];
  loading = false;
  selectedEmployeeId: number | null = null;
  selectedYear: number = new Date().getFullYear();

  constructor(
    private leaveBalanceService: LeaveBalanceService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {}

  loadBalances(): void {
    if (!this.selectedEmployeeId) {
      return;
    }
    this.loading = true;
    this.leaveBalanceService.getBalancesByEmployee(this.selectedEmployeeId, this.selectedYear)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: data => {
          this.balances = data;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load leave balances');
          this.loading = false;
        }
      });
  }

  onSearch(): void {
    this.loadBalances();
  }

  getUsagePercentage(balance: LeaveBalanceResponse): number {
    const total = balance.credited + balance.carryForwarded;
    if (total <= 0) return 0;
    return Math.round((balance.used / total) * 100);
  }
}
