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
import { LeavePolicyAssignmentService } from '../../../services/leave-policy-assignment.service';
import { NotificationService } from '../../../services/notification.service';
import { LeavePolicyAssignmentResponse } from '../../../models/leave-policy-assignment.model';

@Component({
  selector: 'app-leave-policy-assignment-list',
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
  templateUrl: './leave-policy-assignment-list.component.html',
  styleUrl: './leave-policy-assignment-list.component.scss',
})
export class LeavePolicyAssignmentListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'leavePolicyName', 'assignmentType', 'target', 'effectiveFrom', 'effectiveTo', 'active', 'actions'
  ];
  dataSource = new MatTableDataSource<LeavePolicyAssignmentResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  selectedType = '';
  assignmentTypes = ['DEPARTMENT', 'DESIGNATION', 'INDIVIDUAL'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private assignmentService: LeavePolicyAssignmentService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAssignments();
  }

  loadAssignments(): void {
    this.loading = true;

    if (this.selectedType) {
      this.assignmentService.getByType(this.selectedType)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: assignments => {
            this.totalElements = assignments.length;
            this.dataSource.data = assignments.slice(
              this.pageIndex * this.pageSize,
              (this.pageIndex + 1) * this.pageSize
            );
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load policy assignments');
            this.loading = false;
          }
        });
    } else {
      this.assignmentService.getAll(this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load policy assignments');
            this.loading = false;
          }
        });
    }
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadAssignments();
  }

  onSearch(): void {
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadAssignments();
  }

  clearFilters(): void {
    this.selectedType = '';
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadAssignments();
  }

  editAssignment(assignment: LeavePolicyAssignmentResponse): void {
    this.router.navigate(['/leave-policy-assignments', assignment.id, 'edit']);
  }

  toggleActive(assignment: LeavePolicyAssignmentResponse): void {
    this.assignmentService.updateActive(assignment.id, !assignment.active)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success(`Assignment ${assignment.active ? 'deactivated' : 'activated'} successfully`);
          this.loadAssignments();
        },
        error: () => this.notificationService.error('Failed to update assignment status')
      });
  }

  deleteAssignment(assignment: LeavePolicyAssignmentResponse): void {
    if (confirm(`Are you sure you want to delete this policy assignment?`)) {
      this.assignmentService.delete(assignment.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Assignment deleted successfully');
            this.loadAssignments();
          },
          error: () => this.notificationService.error('Failed to delete assignment')
        });
    }
  }
}
