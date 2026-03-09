import { Component, OnInit, ChangeDetectionStrategy, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { UniformService } from '../../../services/uniform.service';
import { NotificationService } from '../../../services/notification.service';
import { UniformResponse } from '../../../models/uniform.model';
import { UniformFormDialogComponent } from '../uniform-form-dialog/uniform-form-dialog.component';

@Component({
  selector: 'app-uniform-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule, MatButtonModule, MatIconModule,
    MatCardModule, MatTooltipModule, MatProgressSpinnerModule,
    MatDialogModule
  ],
  templateUrl: './uniform-list.component.html',
  styleUrl: './uniform-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UniformListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = ['name', 'type', 'size', 'description', 'active', 'actions'];
  dataSource = new MatTableDataSource<UniformResponse>();
  loading = false;

  constructor(
    private uniformService: UniformService,
    private notificationService: NotificationService,
    private dialog: MatDialog,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUniforms();
  }

  loadUniforms(): void {
    this.loading = true;
    this.uniformService.getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: uniforms => {
          this.dataSource.data = uniforms;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load uniforms');
          this.loading = false;
        }
      });
  }

  openFormDialog(uniform?: UniformResponse): void {
    const dialogRef = this.dialog.open(UniformFormDialogComponent, {
      width: '500px',
      data: uniform || null
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) this.loadUniforms();
    });
  }

  deleteUniform(uniform: UniformResponse): void {
    if (confirm(`Are you sure you want to delete uniform "${uniform.name}"?`)) {
      this.uniformService.delete(uniform.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Uniform deleted successfully');
            this.loadUniforms();
          },
          error: () => this.notificationService.error('Failed to delete uniform')
        });
    }
  }

  goToAllocations(): void {
    this.router.navigate(['/uniforms/allocations']);
  }
}
