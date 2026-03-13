import { Component, OnInit, ViewChild, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { SalaryStructureService } from '../../../services/salary-structure.service';
import { NotificationService } from '../../../services/notification.service';
import { SalaryStructureResponse } from '../../../models/salary-structure.model';

@Component({
  selector: 'app-salary-structure-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    MatDialogModule
  ],
  templateUrl: './salary-structure-list.component.html',
  styleUrl: './salary-structure-list.component.scss',
})
export class SalaryStructureListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'code', 'name', 'components', 'active', 'actions'
  ];
  dataSource = new MatTableDataSource<SalaryStructureResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private salaryStructureService: SalaryStructureService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadStructures();
  }

  loadStructures(): void {
    this.loading = true;
    this.salaryStructureService.getAll(this.pageIndex, this.pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          this.dataSource.data = page.content;
          this.totalElements = page.totalElements;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load salary structures');
          this.loading = false;
        }
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadStructures();
  }

  editStructure(structure: SalaryStructureResponse): void {
    this.router.navigate(['/salary-structures', structure.id, 'edit']);
  }

  cloneStructure(structure: SalaryStructureResponse): void {
    const newCode = prompt('Enter code for the cloned structure:');
    const newName = prompt('Enter name for the cloned structure:');
    if (newCode && newName) {
      this.salaryStructureService.clone(structure.id, { newCode, newName })
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Structure cloned successfully');
            this.loadStructures();
          },
          error: () => this.notificationService.error('Failed to clone structure')
        });
    }
  }

  toggleActive(structure: SalaryStructureResponse): void {
    this.salaryStructureService.updateActive(structure.id, !structure.active)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success(`Structure ${structure.active ? 'deactivated' : 'activated'} successfully`);
          this.loadStructures();
        },
        error: () => this.notificationService.error('Failed to update structure status')
      });
  }

  deleteStructure(structure: SalaryStructureResponse): void {
    if (confirm(`Are you sure you want to delete structure "${structure.name}"?`)) {
      this.salaryStructureService.delete(structure.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Structure deleted successfully');
            this.loadStructures();
          },
          error: () => this.notificationService.error('Failed to delete structure')
        });
    }
  }
}
