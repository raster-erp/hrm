import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PromotionService } from '../../../services/promotion.service';
import { NotificationService } from '../../../services/notification.service';
import { PromotionResponse } from '../../../models/promotion.model';

@Component({
  selector: 'app-promotion-detail',
  standalone: true,
  imports: [
    CommonModule, MatButtonModule, MatIconModule,
    MatCardModule, MatProgressSpinnerModule
  ],
  templateUrl: './promotion-detail.component.html',
  styleUrl: './promotion-detail.component.scss',
})
export class PromotionDetailComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  promotion: PromotionResponse | null = null;
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private promotionService: PromotionService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadPromotion(+id);
    } else {
      this.router.navigate(['/promotions']);
    }
  }

  loadPromotion(id: number): void {
    this.loading = true;
    this.promotionService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: promotion => {
          this.promotion = promotion;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load promotion details');
          this.loading = false;
          this.router.navigate(['/promotions']);
        }
      });
  }

  approve(): void {
    if (!this.promotion) return;
    this.promotionService.approve(this.promotion.id, 1)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: updated => {
          this.promotion = updated;
          this.notificationService.success('Promotion approved');
        },
        error: () => this.notificationService.error('Failed to approve promotion')
      });
  }

  reject(): void {
    if (!this.promotion) return;
    this.promotionService.reject(this.promotion.id, 1)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: updated => {
          this.promotion = updated;
          this.notificationService.success('Promotion rejected');
        },
        error: () => this.notificationService.error('Failed to reject promotion')
      });
  }

  execute(): void {
    if (!this.promotion) return;
    this.promotionService.execute(this.promotion.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: updated => {
          this.promotion = updated;
          this.notificationService.success('Promotion executed');
        },
        error: () => this.notificationService.error('Failed to execute promotion')
      });
  }

  goBack(): void {
    this.router.navigate(['/promotions']);
  }
}
