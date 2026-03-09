import { Component, OnInit, ChangeDetectionStrategy, ViewChild, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { IdCardService } from '../../../services/id-card.service';
import { NotificationService } from '../../../services/notification.service';
import { IdCardResponse } from '../../../models/id-card.model';

@Component({
  selector: 'app-id-card-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatTooltipModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './id-card-list.component.html',
  styleUrl: './id-card-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class IdCardListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = ['cardNumber', 'employeeName', 'employeeCode', 'issueDate', 'expiryDate', 'status', 'actions'];
  dataSource = new MatTableDataSource<IdCardResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private idCardService: IdCardService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadIdCards();
  }

  loadIdCards(): void {
    this.loading = true;
    this.idCardService.getAll(this.pageIndex, this.pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          this.dataSource.data = page.content;
          this.totalElements = page.totalElements;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load ID cards');
          this.loading = false;
        }
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadIdCards();
  }

  viewIdCard(idCard: IdCardResponse): void {
    this.router.navigate(['/id-cards', idCard.id]);
  }

  editIdCard(idCard: IdCardResponse): void {
    this.router.navigate(['/id-cards', idCard.id, 'edit']);
  }

  deleteIdCard(idCard: IdCardResponse): void {
    if (confirm(`Are you sure you want to delete ID card "${idCard.cardNumber}"?`)) {
      this.idCardService.delete(idCard.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('ID card deleted successfully');
            this.loadIdCards();
          },
          error: () => this.notificationService.error('Failed to delete ID card')
        });
    }
  }
}
