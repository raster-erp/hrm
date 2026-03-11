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
import { LeavePolicyService } from '../../../services/leave-policy.service';
import { NotificationService } from '../../../services/notification.service';
import { LeavePolicyResponse } from '../../../models/leave-policy.model';

@Component({
  selector: 'app-leave-policy-list',
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
  templateUrl: './leave-policy-list.component.html',
  styleUrl: './leave-policy-list.component.scss',
})
export class LeavePolicyListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'name', 'leaveTypeName', 'accrualFrequency', 'accrualDays', 'maxAccumulation', 'carryForwardLimit', 'active', 'actions'
  ];
  dataSource = new MatTableDataSource<LeavePolicyResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private leavePolicyService: LeavePolicyService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadPolicies();
  }

  loadPolicies(): void {
    this.loading = true;
    this.leavePolicyService.getAll(this.pageIndex, this.pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          this.dataSource.data = page.content;
          this.totalElements = page.totalElements;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load leave policies');
          this.loading = false;
        }
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadPolicies();
  }

  editPolicy(policy: LeavePolicyResponse): void {
    this.router.navigate(['/leave-policies', policy.id, 'edit']);
  }

  toggleActive(policy: LeavePolicyResponse): void {
    this.leavePolicyService.updateActive(policy.id, !policy.active)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success(`Policy ${policy.active ? 'deactivated' : 'activated'} successfully`);
          this.loadPolicies();
        },
        error: () => this.notificationService.error('Failed to update policy status')
      });
  }

  deletePolicy(policy: LeavePolicyResponse): void {
    if (confirm(`Are you sure you want to delete policy "${policy.name}"?`)) {
      this.leavePolicyService.delete(policy.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Policy deleted successfully');
            this.loadPolicies();
          },
          error: () => this.notificationService.error('Failed to delete policy')
        });
    }
  }
}
