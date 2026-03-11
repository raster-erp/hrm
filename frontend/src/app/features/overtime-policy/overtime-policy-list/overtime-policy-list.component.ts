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
import { OvertimePolicyService } from '../../../services/overtime-policy.service';
import { NotificationService } from '../../../services/notification.service';
import { OvertimePolicyResponse } from '../../../models/overtime-policy.model';

@Component({
  selector: 'app-overtime-policy-list',
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
  templateUrl: './overtime-policy-list.component.html',
  styleUrl: './overtime-policy-list.component.scss',
})
export class OvertimePolicyListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'name', 'type', 'rateMultiplier', 'minOvertimeMinutes', 'maxOvertimeMinutesPerDay', 'requiresApproval', 'active', 'actions'
  ];
  dataSource = new MatTableDataSource<OvertimePolicyResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  selectedType = '';
  types = ['WEEKDAY', 'WEEKEND', 'HOLIDAY', 'DOUBLE_TIME'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private overtimePolicyService: OvertimePolicyService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadPolicies();
  }

  loadPolicies(): void {
    this.loading = true;

    if (this.selectedType) {
      this.overtimePolicyService.getByType(this.selectedType)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: policies => {
            this.totalElements = policies.length;
            this.dataSource.data = policies.slice(
              this.pageIndex * this.pageSize,
              (this.pageIndex + 1) * this.pageSize
            );
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load overtime policies');
            this.loading = false;
          }
        });
    } else {
      this.overtimePolicyService.getAll(this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load overtime policies');
            this.loading = false;
          }
        });
    }
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadPolicies();
  }

  onSearch(): void {
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadPolicies();
  }

  clearFilters(): void {
    this.selectedType = '';
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadPolicies();
  }

  editPolicy(policy: OvertimePolicyResponse): void {
    this.router.navigate(['/overtime-policies', policy.id, 'edit']);
  }

  toggleActive(policy: OvertimePolicyResponse): void {
    this.overtimePolicyService.updateActive(policy.id, !policy.active)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success(`Policy ${policy.active ? 'deactivated' : 'activated'} successfully`);
          this.loadPolicies();
        },
        error: () => this.notificationService.error('Failed to update policy status')
      });
  }

  deletePolicy(policy: OvertimePolicyResponse): void {
    if (confirm(`Are you sure you want to delete policy "${policy.name}"?`)) {
      this.overtimePolicyService.delete(policy.id)
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
