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
import { EmployeeSalaryService } from '../../../services/employee-salary.service';
import { NotificationService } from '../../../services/notification.service';
import { EmployeeSalaryDetailResponse } from '../../../models/employee-salary.model';

@Component({
  selector: 'app-employee-salary-list',
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
    MatProgressSpinnerModule
  ],
  templateUrl: './employee-salary-list.component.html',
  styleUrl: './employee-salary-list.component.scss',
})
export class EmployeeSalaryListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'employeeCode', 'employeeName', 'salaryStructure', 'ctc', 'basicSalary', 'effectiveDate', 'active', 'actions'
  ];
  dataSource = new MatTableDataSource<EmployeeSalaryDetailResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private employeeSalaryService: EmployeeSalaryService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDetails();
  }

  loadDetails(): void {
    this.loading = true;
    this.employeeSalaryService.getAll(this.pageIndex, this.pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          this.dataSource.data = page.content;
          this.totalElements = page.totalElements;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load salary assignments');
          this.loading = false;
        }
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadDetails();
  }

  editDetail(detail: EmployeeSalaryDetailResponse): void {
    this.router.navigate(['/employee-salary', detail.id, 'edit']);
  }

  deleteDetail(detail: EmployeeSalaryDetailResponse): void {
    if (confirm(`Are you sure you want to delete this salary assignment for "${detail.employeeName}"?`)) {
      this.employeeSalaryService.delete(detail.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Salary assignment deleted successfully');
            this.loadDetails();
          },
          error: () => this.notificationService.error('Failed to delete salary assignment')
        });
    }
  }
}
