import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CredentialService } from '../../../services/credential.service';
import { CredentialResponse } from '../../../models/credential.model';

@Component({
  selector: 'app-credential-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatListModule,
    MatButtonModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './credential-dashboard.component.html',
  styleUrl: './credential-dashboard.component.scss',
})
export class CredentialDashboardComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  expiring30: CredentialResponse[] = [];
  expiring60: CredentialResponse[] = [];
  expiring90: CredentialResponse[] = [];
  loading = false;

  constructor(
    private credentialService: CredentialService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loading = true;
    this.credentialService.getExpiring(30)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: data => { this.expiring30 = data; this.loading = false; },
        error: () => { this.loading = false; }
      });

    this.credentialService.getExpiring(60)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(data => this.expiring60 = data);

    this.credentialService.getExpiring(90)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(data => this.expiring90 = data);
  }

  viewCredential(id: number): void {
    this.router.navigate(['/credentials', id]);
  }

  goBack(): void {
    this.router.navigate(['/credentials']);
  }
}
