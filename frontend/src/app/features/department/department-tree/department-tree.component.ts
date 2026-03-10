import { Component, OnInit, DestroyRef, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTreeModule, MatTreeFlatDataSource, MatTreeFlattener } from '@angular/material/tree';
import { FlatTreeControl } from '@angular/cdk/tree';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { DepartmentService } from '../../../services/department.service';
import { NotificationService } from '../../../services/notification.service';
import { DepartmentResponse } from '../../../models/department.model';
import { DepartmentFormDialogComponent, DepartmentFormDialogData } from '../department-form-dialog/department-form-dialog.component';

interface DepartmentFlatNode {
  id: number;
  name: string;
  code: string;
  active: boolean;
  parentId?: number;
  level: number;
  expandable: boolean;
}

@Component({
  selector: 'app-department-tree',
  standalone: true,
  imports: [
    CommonModule,
    MatTreeModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    MatDialogModule
  ],
  templateUrl: './department-tree.component.html',
  styleUrl: './department-tree.component.scss',
})
export class DepartmentTreeComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  loading = false;

  private transformer = (node: DepartmentResponse, level: number): DepartmentFlatNode => ({
    id: node.id,
    name: node.name,
    code: node.code,
    active: node.active,
    parentId: node.parentId,
    level,
    expandable: !!node.children && node.children.length > 0
  });

  treeControl = new FlatTreeControl<DepartmentFlatNode>(
    node => node.level,
    node => node.expandable
  );

  private treeFlattener = new MatTreeFlattener(
    this.transformer,
    node => node.level,
    node => node.expandable,
    node => node.children
  );

  dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);

  constructor(
    private departmentService: DepartmentService,
    private notificationService: NotificationService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadDepartments();
  }

  hasChild = (_: number, node: DepartmentFlatNode): boolean => node.expandable;

  loadDepartments(): void {
    this.loading = true;
    this.departmentService.getRootDepartments()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (departments) => {
          this.dataSource.data = departments;
          this.loading = false;
          this.cdr.markForCheck();
        },
        error: () => {
          this.notificationService.error('Failed to load departments');
          this.loading = false;
          this.cdr.markForCheck();
        }
      });
  }

  addRootDepartment(): void {
    this.openDialog({ parentId: undefined });
  }

  addChildDepartment(node: DepartmentFlatNode): void {
    this.openDialog({ parentId: node.id });
  }

  editDepartment(node: DepartmentFlatNode): void {
    this.departmentService.getById(node.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (department) => {
          this.openDialog({ department, parentId: department.parentId });
        },
        error: () => {
          this.notificationService.error('Failed to load department details');
        }
      });
  }

  deleteDepartment(node: DepartmentFlatNode): void {
    if (confirm(`Are you sure you want to delete "${node.name}"?`)) {
      this.departmentService.delete(node.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Department deleted successfully');
            this.loadDepartments();
          },
          error: () => {
            this.notificationService.error('Failed to delete department');
          }
        });
    }
  }

  private openDialog(data: DepartmentFormDialogData): void {
    const dialogRef = this.dialog.open(DepartmentFormDialogComponent, {
      width: '500px',
      data
    });

    dialogRef.afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(result => {
        if (!result) return;

        if (data.department) {
          this.departmentService.update(data.department.id, result)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
              next: () => {
                this.notificationService.success('Department updated successfully');
                this.loadDepartments();
              },
              error: () => {
                this.notificationService.error('Failed to update department');
              }
            });
        } else {
          this.departmentService.create(result)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
              next: () => {
                this.notificationService.success('Department created successfully');
                this.loadDepartments();
              },
              error: () => {
                this.notificationService.error('Failed to create department');
              }
            });
        }
      });
  }
}
