import { Component, DestroyRef, inject } from '@angular/core';
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
import { LeaveBalanceService } from '../../../services/leave-balance.service';
import { NotificationService } from '../../../services/notification.service';
import { YearEndSummaryResponse } from '../../../models/leave-balance.model';

@Component({
  selector: 'app-year-end-processing',
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
    MatProgressSpinnerModule
  ],
  templateUrl: './year-end-processing.component.html',
  styleUrl: './year-end-processing.component.scss',
})
export class YearEndProcessingComponent {
  private readonly destroyRef = inject(DestroyRef);

  selectedYear: number = new Date().getFullYear() - 1;
  processedBy = '';
  processing = false;
  summary: YearEndSummaryResponse | null = null;

  constructor(
    private leaveBalanceService: LeaveBalanceService,
    private notificationService: NotificationService
  ) {}

  processYearEnd(): void {
    if (!this.selectedYear) {
      this.notificationService.error('Please enter a year');
      return;
    }

    if (!confirm(`Are you sure you want to process year-end for ${this.selectedYear}? This will carry forward eligible balances and lapse remaining days.`)) {
      return;
    }

    this.processing = true;
    this.summary = null;

    this.leaveBalanceService.processYearEnd({
      year: this.selectedYear,
      processedBy: this.processedBy || null
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: result => {
          this.summary = result;
          this.processing = false;
          this.notificationService.success('Year-end processing completed successfully');
        },
        error: () => {
          this.processing = false;
          this.notificationService.error('Year-end processing failed');
        }
      });
  }
}
