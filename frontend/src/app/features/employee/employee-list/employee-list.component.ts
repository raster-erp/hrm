import { Component, OnInit, ViewChild, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { EmployeeService } from '../../../services/employee.service';
import { DepartmentService } from '../../../services/department.service';
import { NotificationService } from '../../../services/notification.service';
import { EmployeeResponse, EmployeeSearchCriteria } from '../../../models/employee.model';
import { DepartmentResponse } from '../../../models/department.model';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatCardModule,
    MatChipsModule,
    MatTooltipModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './employee-list.component.html',
  styleUrl: './employee-list.component.scss',
})
export class EmployeeListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'employeeCode', 'name', 'email', 'departmentName',
    'designationName', 'employmentStatus', 'actions'
  ];
  dataSource = new MatTableDataSource<EmployeeResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  searchName = '';
  selectedDepartmentId: number | null = null;
  selectedStatus = '';
  joiningDateFrom: Date | null = null;
  joiningDateTo: Date | null = null;

  departments: DepartmentResponse[] = [];
  statuses = ['ACTIVE', 'INACTIVE', 'ON_LEAVE', 'TERMINATED'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private employeeService: EmployeeService,
    private departmentService: DepartmentService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadEmployees();
    this.loadDepartments();
  }

  loadEmployees(): void {
    this.loading = true;
    const hasFilters = this.searchName || this.selectedDepartmentId ||
      this.selectedStatus || this.joiningDateFrom || this.joiningDateTo;

    if (hasFilters) {
      const criteria: EmployeeSearchCriteria = {};
      if (this.searchName) criteria.name = this.searchName;
      if (this.selectedDepartmentId) criteria.departmentId = this.selectedDepartmentId;
      if (this.selectedStatus) criteria.status = this.selectedStatus;
      if (this.joiningDateFrom) criteria.joiningDateFrom = this.formatDate(this.joiningDateFrom);
      if (this.joiningDateTo) criteria.joiningDateTo = this.formatDate(this.joiningDateTo);

      this.employeeService.search(criteria, this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to search employees');
            this.loading = false;
          }
        });
    } else {
      this.employeeService.getAll(this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load employees');
            this.loading = false;
          }
        });
    }
  }

  loadDepartments(): void {
    this.departmentService.getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: departments => this.departments = departments,
        error: () => this.notificationService.error('Failed to load departments')
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadEmployees();
  }

  onSearch(): void {
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadEmployees();
  }

  clearFilters(): void {
    this.searchName = '';
    this.selectedDepartmentId = null;
    this.selectedStatus = '';
    this.joiningDateFrom = null;
    this.joiningDateTo = null;
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadEmployees();
  }

  viewEmployee(employee: EmployeeResponse): void {
    this.router.navigate(['/employees', employee.id]);
  }

  editEmployee(employee: EmployeeResponse): void {
    this.router.navigate(['/employees', employee.id, 'edit']);
  }

  deleteEmployee(employee: EmployeeResponse): void {
    if (confirm(`Are you sure you want to delete ${employee.firstName} ${employee.lastName}?`)) {
      this.employeeService.delete(employee.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Employee deleted successfully');
            this.loadEmployees();
          },
          error: () => this.notificationService.error('Failed to delete employee')
        });
    }
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'primary';
      case 'INACTIVE': return 'warn';
      case 'ON_LEAVE': return 'accent';
      case 'TERMINATED': return 'warn';
      default: return '';
    }
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
