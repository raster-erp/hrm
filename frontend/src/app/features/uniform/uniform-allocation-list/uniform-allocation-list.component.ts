import { Component, OnInit, DestroyRef, inject } from '@angular/core';
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
import { UniformAllocationResponse } from '../../../models/uniform.model';
import { UniformAllocationDialogComponent } from '../uniform-allocation-dialog/uniform-allocation-dialog.component';

@Component({
  selector: 'app-uniform-allocation-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule, MatButtonModule, MatIconModule,
    MatCardModule, MatTooltipModule, MatProgressSpinnerModule,
    MatDialogModule
  ],
  templateUrl: './uniform-allocation-list.component.html',
  styleUrl: './uniform-allocation-list.component.scss',
})
export class UniformAllocationListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = ['employeeName', 'uniformName', 'allocatedDate', 'returnedDate', 'quantity', 'status', 'actions'];
  dataSource = new MatTableDataSource<UniformAllocationResponse>();
  loading = false;

  constructor(
    private uniformService: UniformService,
    private notificationService: NotificationService,
    private dialog: MatDialog,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAllocations();
  }

  loadAllocations(): void {
    this.loading = true;
    this.uniformService.getAllAllocations()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: allocations => {
          this.dataSource.data = allocations;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load allocations');
          this.loading = false;
        }
      });
  }

  openAllocationDialog(): void {
    const dialogRef = this.dialog.open(UniformAllocationDialogComponent, {
      width: '500px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) this.loadAllocations();
    });
  }

  returnUniform(allocation: UniformAllocationResponse): void {
    if (confirm(`Mark uniform "${allocation.uniformName}" as returned?`)) {
      this.uniformService.returnUniform(allocation.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Uniform marked as returned');
            this.loadAllocations();
          },
          error: () => this.notificationService.error('Failed to return uniform')
        });
    }
  }

  goBack(): void {
    this.router.navigate(['/uniforms']);
  }
}
