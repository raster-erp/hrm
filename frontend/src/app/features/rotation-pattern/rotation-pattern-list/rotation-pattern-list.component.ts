import { Component, OnInit, ViewChild, DestroyRef, inject } from '@angular/core';
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
import { RotationPatternService } from '../../../services/rotation-pattern.service';
import { NotificationService } from '../../../services/notification.service';
import { RotationPatternResponse } from '../../../models/rotation-pattern.model';

@Component({
  selector: 'app-rotation-pattern-list',
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
  templateUrl: './rotation-pattern-list.component.html',
  styleUrl: './rotation-pattern-list.component.scss',
})
export class RotationPatternListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = ['name', 'description', 'rotationDays', 'shiftSequence', 'actions'];
  dataSource = new MatTableDataSource<RotationPatternResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private rotationPatternService: RotationPatternService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadPatterns();
  }

  loadPatterns(): void {
    this.loading = true;
    this.rotationPatternService.getAll(this.pageIndex, this.pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          this.dataSource.data = page.content;
          this.totalElements = page.totalElements;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load rotation patterns');
          this.loading = false;
        }
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadPatterns();
  }

  editPattern(pattern: RotationPatternResponse): void {
    this.router.navigate(['/rotation-patterns', pattern.id, 'edit']);
  }

  deletePattern(pattern: RotationPatternResponse): void {
    if (confirm(`Are you sure you want to delete rotation pattern "${pattern.name}"?`)) {
      this.rotationPatternService.delete(pattern.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Rotation pattern deleted successfully');
            this.loadPatterns();
          },
          error: () => this.notificationService.error('Failed to delete rotation pattern')
        });
    }
  }
}
