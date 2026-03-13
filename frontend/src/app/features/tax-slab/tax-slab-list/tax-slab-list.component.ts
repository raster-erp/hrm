import { Component, OnInit, ViewChild, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TaxSlabService } from '../../../services/tax-slab.service';
import { NotificationService } from '../../../services/notification.service';
import { TaxSlabResponse } from '../../../models/tax-slab.model';

@Component({
  selector: 'app-tax-slab-list',
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
    MatSelectModule,
    MatInputModule,
    MatCardModule,
    MatChipsModule,
    MatTooltipModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './tax-slab-list.component.html',
  styleUrl: './tax-slab-list.component.scss',
})
export class TaxSlabListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'regime', 'financialYear', 'slabFrom', 'slabTo', 'rate', 'active', 'actions'
  ];
  dataSource = new MatTableDataSource<TaxSlabResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  selectedRegime = '';
  regimes = ['OLD', 'NEW'];
  financialYear = '';

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private taxSlabService: TaxSlabService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadSlabs();
  }

  loadSlabs(): void {
    this.loading = true;

    if (this.selectedRegime && this.financialYear) {
      this.taxSlabService.getByRegimeAndYear(this.selectedRegime, this.financialYear)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: slabs => {
            this.totalElements = slabs.length;
            this.dataSource.data = slabs.slice(
              this.pageIndex * this.pageSize,
              (this.pageIndex + 1) * this.pageSize
            );
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load tax slabs');
            this.loading = false;
          }
        });
    } else {
      this.taxSlabService.getAll(this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load tax slabs');
            this.loading = false;
          }
        });
    }
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadSlabs();
  }

  onSearch(): void {
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadSlabs();
  }

  clearFilters(): void {
    this.selectedRegime = '';
    this.financialYear = '';
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadSlabs();
  }

  editSlab(slab: TaxSlabResponse): void {
    this.router.navigate(['/tax-slabs', slab.id, 'edit']);
  }

  deleteSlab(slab: TaxSlabResponse): void {
    if (confirm(`Are you sure you want to delete this tax slab?`)) {
      this.taxSlabService.delete(slab.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Tax slab deleted successfully');
            this.loadSlabs();
          },
          error: () => this.notificationService.error('Failed to delete tax slab')
        });
    }
  }
}
