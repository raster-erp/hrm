import { Component, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { WfhRequestService } from '../../../services/wfh-request.service';
import { NotificationService } from '../../../services/notification.service';
import { WfhCheckInResponse } from '../../../models/wfh-request.model';

@Component({
  selector: 'app-wfh-checkin',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatCardModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './wfh-checkin.component.html',
  styleUrl: './wfh-checkin.component.scss',
})
export class WfhCheckinComponent {
  private readonly destroyRef = inject(DestroyRef);

  wfhRequestId: number | null = null;
  ipAddress = '';
  location = '';
  activeSession: WfhCheckInResponse | null = null;
  activityLogs = new MatTableDataSource<WfhCheckInResponse>();
  loading = false;
  checking = false;

  displayedColumns: string[] = ['checkInTime', 'checkOutTime', 'ipAddress', 'location'];

  constructor(
    private wfhRequestService: WfhRequestService,
    private notificationService: NotificationService
  ) {}

  loadStatus(): void {
    if (!this.wfhRequestId) {
      this.notificationService.error('Please enter a WFH Request ID');
      return;
    }

    this.loading = true;
    this.activeSession = null;

    this.wfhRequestService.getActiveSession(this.wfhRequestId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: session => {
          this.activeSession = session;
          this.loading = false;
        },
        error: () => {
          this.activeSession = null;
          this.loading = false;
        }
      });

    this.wfhRequestService.getActivityLogs(this.wfhRequestId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: logs => {
          this.activityLogs.data = logs;
        },
        error: () => {
          this.notificationService.error('Failed to load activity logs');
        }
      });
  }

  checkIn(): void {
    if (!this.wfhRequestId) {
      this.notificationService.error('Please enter a WFH Request ID');
      return;
    }

    this.checking = true;
    this.wfhRequestService.checkIn({
      wfhRequestId: this.wfhRequestId,
      ipAddress: this.ipAddress || null,
      location: this.location || null
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: session => {
          this.activeSession = session;
          this.notificationService.success('Checked in successfully');
          this.checking = false;
          this.loadStatus();
        },
        error: () => {
          this.notificationService.error('Failed to check in');
          this.checking = false;
        }
      });
  }

  checkOut(): void {
    if (!this.activeSession) {
      return;
    }

    this.checking = true;
    this.wfhRequestService.checkOut(this.activeSession.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.activeSession = null;
          this.notificationService.success('Checked out successfully');
          this.checking = false;
          this.loadStatus();
        },
        error: () => {
          this.notificationService.error('Failed to check out');
          this.checking = false;
        }
      });
  }
}
