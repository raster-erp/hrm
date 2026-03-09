import { Component, OnInit, ChangeDetectionStrategy, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { IdCardService } from '../../../services/id-card.service';
import { NotificationService } from '../../../services/notification.service';
import { IdCardResponse } from '../../../models/id-card.model';

@Component({
  selector: 'app-id-card-detail',
  standalone: true,
  imports: [
    CommonModule, MatButtonModule, MatIconModule,
    MatCardModule, MatMenuModule, MatProgressSpinnerModule
  ],
  templateUrl: './id-card-detail.component.html',
  styleUrl: './id-card-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class IdCardDetailComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  idCard: IdCardResponse | null = null;
  loading = false;
  statuses = ['ACTIVE', 'EXPIRED', 'CANCELLED'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private idCardService: IdCardService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadIdCard(+id);
    } else {
      this.router.navigate(['/id-cards']);
    }
  }

  loadIdCard(id: number): void {
    this.loading = true;
    this.idCardService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: idCard => {
          this.idCard = idCard;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load ID card details');
          this.loading = false;
          this.router.navigate(['/id-cards']);
        }
      });
  }

  updateStatus(status: string): void {
    if (!this.idCard) return;
    this.idCardService.updateStatus(this.idCard.id, status)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: updated => {
          this.idCard = updated;
          this.notificationService.success(`Status updated to ${status}`);
        },
        error: () => this.notificationService.error('Failed to update status')
      });
  }

  editIdCard(): void {
    if (this.idCard) {
      this.router.navigate(['/id-cards', this.idCard.id, 'edit']);
    }
  }

  goBack(): void {
    this.router.navigate(['/id-cards']);
  }
}
