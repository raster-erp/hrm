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
import { CredentialService } from '../../../services/credential.service';
import { NotificationService } from '../../../services/notification.service';
import { CredentialResponse } from '../../../models/credential.model';

@Component({
  selector: 'app-credential-list',
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
  templateUrl: './credential-list.component.html',
  styleUrl: './credential-list.component.scss',
})
export class CredentialListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'employeeName', 'credentialType', 'credentialName', 'issuingAuthority',
    'issueDate', 'expiryDate', 'verificationStatus', 'actions'
  ];
  dataSource = new MatTableDataSource<CredentialResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  selectedStatus = '';
  statuses = ['VERIFIED', 'PENDING', 'EXPIRED'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private credentialService: CredentialService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCredentials();
  }

  loadCredentials(): void {
    this.loading = true;

    if (this.selectedStatus) {
      this.credentialService.getByStatus(this.selectedStatus)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: credentials => {
            this.dataSource.data = credentials;
            this.totalElements = credentials.length;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load credentials');
            this.loading = false;
          }
        });
    } else {
      this.credentialService.getAll(this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load credentials');
            this.loading = false;
          }
        });
    }
  }

  onStatusFilter(): void {
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadCredentials();
  }

  clearFilters(): void {
    this.selectedStatus = '';
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadCredentials();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadCredentials();
  }

  viewCredential(credential: CredentialResponse): void {
    this.router.navigate(['/credentials', credential.id]);
  }

  editCredential(credential: CredentialResponse): void {
    this.router.navigate(['/credentials', credential.id, 'edit']);
  }

  deleteCredential(credential: CredentialResponse): void {
    if (confirm(`Are you sure you want to delete credential "${credential.credentialName}"?`)) {
      this.credentialService.delete(credential.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Credential deleted successfully');
            this.loadCredentials();
          },
          error: () => this.notificationService.error('Failed to delete credential')
        });
    }
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'VERIFIED': return 'primary';
      case 'PENDING': return 'accent';
      case 'EXPIRED': return 'warn';
      default: return '';
    }
  }
}
