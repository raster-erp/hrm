import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { EmployeeService } from '../../../services/employee.service';
import { NotificationService } from '../../../services/notification.service';
import { EmployeeResponse, EmployeeDocumentResponse } from '../../../models/employee.model';

@Component({
  selector: 'app-employee-detail',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    MatTabsModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatListModule,
    MatDividerModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatTooltipModule
  ],
  templateUrl: './employee-detail.component.html',
  styleUrl: './employee-detail.component.scss',
})
export class EmployeeDetailComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  employee: EmployeeResponse | null = null;
  documents: EmployeeDocumentResponse[] = [];
  loading = true;
  documentsLoading = false;

  documentColumns: string[] = ['documentType', 'contentType', 'fileSize', 'createdAt'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private employeeService: EmployeeService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadEmployee(+id);
      this.loadDocuments(+id);
    } else {
      this.router.navigate(['/employees']);
    }
  }

  loadEmployee(id: number): void {
    this.loading = true;
    this.employeeService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: employee => {
          this.employee = employee;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load employee details');
          this.loading = false;
          this.router.navigate(['/employees']);
        }
      });
  }

  loadDocuments(id: number): void {
    this.documentsLoading = true;
    this.employeeService.getDocuments(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: documents => {
          this.documents = documents;
          this.documentsLoading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load documents');
          this.documentsLoading = false;
        }
      });
  }

  editEmployee(): void {
    if (this.employee) {
      this.router.navigate(['/employees', this.employee.id, 'edit']);
    }
  }

  goBack(): void {
    this.router.navigate(['/employees']);
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
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
}
