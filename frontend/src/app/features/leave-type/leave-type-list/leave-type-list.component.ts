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
import { LeaveTypeService } from '../../../services/leave-type.service';
import { NotificationService } from '../../../services/notification.service';
import { LeaveTypeResponse } from '../../../models/leave-type.model';

@Component({
  selector: 'app-leave-type-list',
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
  templateUrl: './leave-type-list.component.html',
  styleUrl: './leave-type-list.component.scss',
})
export class LeaveTypeListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'code', 'name', 'category', 'active', 'actions'
  ];
  dataSource = new MatTableDataSource<LeaveTypeResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  selectedCategory = '';
  categories = ['PAID', 'UNPAID', 'STATUTORY', 'SPECIAL'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private leaveTypeService: LeaveTypeService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadLeaveTypes();
  }

  loadLeaveTypes(): void {
    this.loading = true;

    if (this.selectedCategory) {
      this.leaveTypeService.getByCategory(this.selectedCategory)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: leaveTypes => {
            this.totalElements = leaveTypes.length;
            this.dataSource.data = leaveTypes.slice(
              this.pageIndex * this.pageSize,
              (this.pageIndex + 1) * this.pageSize
            );
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load leave types');
            this.loading = false;
          }
        });
    } else {
      this.leaveTypeService.getAll(this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load leave types');
            this.loading = false;
          }
        });
    }
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadLeaveTypes();
  }

  onSearch(): void {
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadLeaveTypes();
  }

  clearFilters(): void {
    this.selectedCategory = '';
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadLeaveTypes();
  }

  editLeaveType(leaveType: LeaveTypeResponse): void {
    this.router.navigate(['/leave-types', leaveType.id, 'edit']);
  }

  toggleActive(leaveType: LeaveTypeResponse): void {
    this.leaveTypeService.updateActive(leaveType.id, !leaveType.active)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success(`Leave type ${leaveType.active ? 'deactivated' : 'activated'} successfully`);
          this.loadLeaveTypes();
        },
        error: () => this.notificationService.error('Failed to update leave type status')
      });
  }

  deleteLeaveType(leaveType: LeaveTypeResponse): void {
    if (confirm(`Are you sure you want to delete leave type "${leaveType.name}"?`)) {
      this.leaveTypeService.delete(leaveType.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Leave type deleted successfully');
            this.loadLeaveTypes();
          },
          error: () => this.notificationService.error('Failed to delete leave type')
        });
    }
  }
}
