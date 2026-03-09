import { Component, OnInit, ChangeDetectionStrategy, DestroyRef, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { DesignationService } from '../../../services/designation.service';
import { DepartmentService } from '../../../services/department.service';
import { NotificationService } from '../../../services/notification.service';
import { DesignationResponse } from '../../../models/designation.model';
import { DepartmentResponse } from '../../../models/department.model';
import { DesignationFormDialogComponent, DesignationFormDialogData } from '../designation-form-dialog/designation-form-dialog.component';

@Component({
  selector: 'app-designation-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule,
    MatCardModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    MatDialogModule
  ],
  templateUrl: './designation-list.component.html',
  styleUrl: './designation-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DesignationListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  displayedColumns: string[] = ['title', 'code', 'level', 'grade', 'departmentName', 'actions'];
  dataSource = new MatTableDataSource<DesignationResponse>();
  loading = false;

  departments: DepartmentResponse[] = [];
  selectedDepartmentId: number | null = null;

  constructor(
    private designationService: DesignationService,
    private departmentService: DepartmentService,
    private notificationService: NotificationService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadDepartments();
    this.loadDesignations();
  }

  loadDepartments(): void {
    this.departmentService.getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (departments) => {
          this.departments = departments;
          this.cdr.markForCheck();
        },
        error: () => this.notificationService.error('Failed to load departments')
      });
  }

  loadDesignations(): void {
    this.loading = true;

    const source$ = this.selectedDepartmentId
      ? this.designationService.getByDepartment(this.selectedDepartmentId)
      : this.designationService.getAll();

    source$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (designations) => {
          this.dataSource.data = designations;
          this.loading = false;
          this.cdr.markForCheck();
        },
        error: () => {
          this.notificationService.error('Failed to load designations');
          this.loading = false;
          this.cdr.markForCheck();
        }
      });
  }

  onDepartmentFilterChange(): void {
    this.loadDesignations();
  }

  addDesignation(): void {
    this.openDialog({});
  }

  editDesignation(designation: DesignationResponse): void {
    this.openDialog({ designation });
  }

  deleteDesignation(designation: DesignationResponse): void {
    if (confirm(`Are you sure you want to delete "${designation.title}"?`)) {
      this.designationService.delete(designation.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Designation deleted successfully');
            this.loadDesignations();
          },
          error: () => {
            this.notificationService.error('Failed to delete designation');
          }
        });
    }
  }

  private openDialog(data: DesignationFormDialogData): void {
    const dialogRef = this.dialog.open(DesignationFormDialogComponent, {
      width: '550px',
      data
    });

    dialogRef.afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(result => {
        if (!result) return;

        if (data.designation) {
          this.designationService.update(data.designation.id, result)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
              next: () => {
                this.notificationService.success('Designation updated successfully');
                this.loadDesignations();
              },
              error: () => {
                this.notificationService.error('Failed to update designation');
              }
            });
        } else {
          this.designationService.create(result)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
              next: () => {
                this.notificationService.success('Designation created successfully');
                this.loadDesignations();
              },
              error: () => {
                this.notificationService.error('Failed to create designation');
              }
            });
        }
      });
  }
}
