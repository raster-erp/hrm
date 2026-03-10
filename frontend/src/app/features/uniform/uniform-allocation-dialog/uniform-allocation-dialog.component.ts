import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { UniformService } from '../../../services/uniform.service';
import { EmployeeService } from '../../../services/employee.service';
import { NotificationService } from '../../../services/notification.service';
import { UniformResponse, UniformAllocationRequest } from '../../../models/uniform.model';
import { EmployeeResponse } from '../../../models/employee.model';

@Component({
  selector: 'app-uniform-allocation-dialog',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatDialogModule, MatButtonModule, MatInputModule,
    MatFormFieldModule, MatSelectModule, MatDatepickerModule,
    MatNativeDateModule, MatProgressSpinnerModule
  ],
  templateUrl: './uniform-allocation-dialog.component.html',
  styleUrl: './uniform-allocation-dialog.component.scss',
})
export class UniformAllocationDialogComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  allocationForm!: FormGroup;
  saving = false;
  employees: EmployeeResponse[] = [];
  uniforms: UniformResponse[] = [];

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<UniformAllocationDialogComponent>,
    private uniformService: UniformService,
    private employeeService: EmployeeService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.allocationForm = this.fb.group({
      employeeId: [null, Validators.required],
      uniformId: [null, Validators.required],
      allocatedDate: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]]
    });

    this.loadData();
  }

  loadData(): void {
    this.employeeService.getAll(0, 1000)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(page => this.employees = page.content);

    this.uniformService.getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(uniforms => this.uniforms = uniforms.filter(u => u.active));
  }

  onSubmit(): void {
    if (this.allocationForm.invalid) {
      this.allocationForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    const formValue = this.allocationForm.value;

    const request: UniformAllocationRequest = {
      employeeId: formValue.employeeId,
      uniformId: formValue.uniformId,
      allocatedDate: this.formatDate(formValue.allocatedDate),
      quantity: formValue.quantity
    };

    this.uniformService.allocate(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Uniform allocated successfully');
          this.saving = false;
          this.dialogRef.close(true);
        },
        error: () => {
          this.notificationService.error('Failed to allocate uniform');
          this.saving = false;
        }
      });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }

  private formatDate(date: string | Date): string {
    if (typeof date === 'string') return date;
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
