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
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { SalaryComponentService } from '../../../services/salary-component.service';
import { NotificationService } from '../../../services/notification.service';
import { SalaryComponentResponse } from '../../../models/salary-component.model';

@Component({
  selector: 'app-salary-component-list',
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
    MatCardModule,
    MatChipsModule,
    MatTooltipModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './salary-component-list.component.html',
  styleUrl: './salary-component-list.component.scss',
})
export class SalaryComponentListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'code', 'name', 'type', 'computationType', 'taxable', 'active', 'actions'
  ];
  dataSource = new MatTableDataSource<SalaryComponentResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  selectedType = '';
  types = ['EARNING', 'DEDUCTION'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private salaryComponentService: SalaryComponentService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadComponents();
  }

  loadComponents(): void {
    this.loading = true;

    if (this.selectedType) {
      this.salaryComponentService.getByType(this.selectedType)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: components => {
            this.totalElements = components.length;
            this.dataSource.data = components.slice(
              this.pageIndex * this.pageSize,
              (this.pageIndex + 1) * this.pageSize
            );
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load salary components');
            this.loading = false;
          }
        });
    } else {
      this.salaryComponentService.getAll(this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load salary components');
            this.loading = false;
          }
        });
    }
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadComponents();
  }

  onSearch(): void {
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadComponents();
  }

  clearFilters(): void {
    this.selectedType = '';
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadComponents();
  }

  editComponent(component: SalaryComponentResponse): void {
    this.router.navigate(['/salary-components', component.id, 'edit']);
  }

  toggleActive(component: SalaryComponentResponse): void {
    this.salaryComponentService.updateActive(component.id, !component.active)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success(`Component ${component.active ? 'deactivated' : 'activated'} successfully`);
          this.loadComponents();
        },
        error: () => this.notificationService.error('Failed to update component status')
      });
  }

  deleteComponent(component: SalaryComponentResponse): void {
    if (confirm(`Are you sure you want to delete component "${component.name}"?`)) {
      this.salaryComponentService.delete(component.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Component deleted successfully');
            this.loadComponents();
          },
          error: () => this.notificationService.error('Failed to delete component')
        });
    }
  }
}
