import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TransferService } from '../../../services/transfer.service';
import { NotificationService } from '../../../services/notification.service';
import { TransferResponse } from '../../../models/transfer.model';

@Component({
  selector: 'app-transfer-detail',
  standalone: true,
  imports: [
    CommonModule, MatButtonModule, MatIconModule,
    MatCardModule, MatProgressSpinnerModule
  ],
  templateUrl: './transfer-detail.component.html',
  styleUrl: './transfer-detail.component.scss',
})
export class TransferDetailComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  transfer: TransferResponse | null = null;
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private transferService: TransferService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadTransfer(+id);
    } else {
      this.router.navigate(['/transfers']);
    }
  }

  loadTransfer(id: number): void {
    this.loading = true;
    this.transferService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: transfer => {
          this.transfer = transfer;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load transfer details');
          this.loading = false;
          this.router.navigate(['/transfers']);
        }
      });
  }

  approve(): void {
    if (!this.transfer) return;
    this.transferService.approve(this.transfer.id, 1)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: updated => {
          this.transfer = updated;
          this.notificationService.success('Transfer approved');
        },
        error: () => this.notificationService.error('Failed to approve transfer')
      });
  }

  reject(): void {
    if (!this.transfer) return;
    this.transferService.reject(this.transfer.id, 1)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: updated => {
          this.transfer = updated;
          this.notificationService.success('Transfer rejected');
        },
        error: () => this.notificationService.error('Failed to reject transfer')
      });
  }

  execute(): void {
    if (!this.transfer) return;
    this.transferService.execute(this.transfer.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: updated => {
          this.transfer = updated;
          this.notificationService.success('Transfer executed');
        },
        error: () => this.notificationService.error('Failed to execute transfer')
      });
  }

  goBack(): void {
    this.router.navigate(['/transfers']);
  }
}
