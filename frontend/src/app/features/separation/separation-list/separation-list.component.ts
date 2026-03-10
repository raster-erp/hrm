import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { SeparationService } from '../../../services/separation.service';
import { SeparationResponse } from '../../../models/separation.model';

@Component({
  selector: 'app-separation-list',
  standalone: true,
  imports: [
    CommonModule, MatButtonModule, MatIconModule,
    MatTableModule, MatPaginatorModule, MatProgressSpinnerModule
  ],
  templateUrl: './separation-list.component.html',
  styleUrl: './separation-list.component.scss',
})
export class SeparationListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns = ['employeeName', 'separationType', 'noticeDate', 'lastWorkingDay', 'status', 'actions'];
  separations: SeparationResponse[] = [];
  totalElements = 0;
  pageSize = 10;
  loading = false;

  constructor(
    private separationService: SeparationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadSeparations(0, this.pageSize);
  }

  loadSeparations(page: number, size: number): void {
    this.loading = true;
    this.separationService.getAll(page, size)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: result => {
          this.separations = result.content;
          this.totalElements = result.totalElements;
          this.loading = false;
        },
        error: () => this.loading = false
      });
  }

  onPage(event: PageEvent): void {
    this.loadSeparations(event.pageIndex, event.pageSize);
  }

  viewDetail(separation: SeparationResponse): void {
    this.router.navigate(['/separations', separation.id]);
  }

  createNew(): void {
    this.router.navigate(['/separations', 'new']);
  }
}
