import { Component, OnInit, ChangeDetectionStrategy, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { CredentialService } from '../../../services/credential.service';
import { NotificationService } from '../../../services/notification.service';
import { CredentialResponse, CredentialAttachmentResponse } from '../../../models/credential.model';

@Component({
  selector: 'app-credential-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatTableModule,
    MatMenuModule,
    MatProgressSpinnerModule,
    MatTooltipModule
  ],
  templateUrl: './credential-detail.component.html',
  styleUrl: './credential-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CredentialDetailComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  credential: CredentialResponse | null = null;
  attachments: CredentialAttachmentResponse[] = [];
  attachmentDataSource = new MatTableDataSource<CredentialAttachmentResponse>();
  attachmentColumns = ['fileName', 'contentType', 'fileSize', 'createdAt'];
  loading = false;
  attachmentsLoading = false;

  statuses = ['VERIFIED', 'PENDING', 'EXPIRED'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private credentialService: CredentialService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadCredential(+id);
      this.loadAttachments(+id);
    } else {
      this.router.navigate(['/credentials']);
    }
  }

  loadCredential(id: number): void {
    this.loading = true;
    this.credentialService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: credential => {
          this.credential = credential;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load credential details');
          this.loading = false;
          this.router.navigate(['/credentials']);
        }
      });
  }

  loadAttachments(id: number): void {
    this.attachmentsLoading = true;
    this.credentialService.getAttachments(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: attachments => {
          this.attachments = attachments;
          this.attachmentDataSource.data = attachments;
          this.attachmentsLoading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load attachments');
          this.attachmentsLoading = false;
        }
      });
  }

  updateStatus(status: string): void {
    if (!this.credential) return;
    this.credentialService.updateStatus(this.credential.id, status)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: updated => {
          this.credential = updated;
          this.notificationService.success(`Status updated to ${status}`);
        },
        error: () => this.notificationService.error('Failed to update status')
      });
  }

  editCredential(): void {
    if (this.credential) {
      this.router.navigate(['/credentials', this.credential.id, 'edit']);
    }
  }

  goBack(): void {
    this.router.navigate(['/credentials']);
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'VERIFIED': return 'primary';
      case 'PENDING': return 'accent';
      case 'EXPIRED': return 'warn';
      default: return '';
    }
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
}
