import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { DepartmentRequest, DepartmentResponse } from '../../../models/department.model';

export interface DepartmentFormDialogData {
  department?: DepartmentResponse;
  parentId?: number;
}

@Component({
  selector: 'app-department-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCheckboxModule
  ],
  templateUrl: './department-form-dialog.component.html',
  styleUrl: './department-form-dialog.component.scss',
})
export class DepartmentFormDialogComponent {
  form: FormGroup;
  isEditMode: boolean;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<DepartmentFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DepartmentFormDialogData
  ) {
    this.isEditMode = !!data.department;

    this.form = this.fb.group({
      name: [data.department?.name || '', [Validators.required, Validators.maxLength(100)]],
      code: [data.department?.code || '', [Validators.required, Validators.maxLength(20)]],
      description: [data.department?.description || '', [Validators.maxLength(500)]],
      active: [data.department?.active ?? true]
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const request: DepartmentRequest = {
      name: this.form.value.name,
      code: this.form.value.code,
      description: this.form.value.description,
      active: this.form.value.active,
      parentId: this.data.parentId
    };

    this.dialogRef.close(request);
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
