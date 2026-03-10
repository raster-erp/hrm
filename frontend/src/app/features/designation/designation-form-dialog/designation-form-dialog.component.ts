import { Component, Inject, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { DepartmentService } from '../../../services/department.service';
import { DepartmentResponse } from '../../../models/department.model';
import { DesignationRequest, DesignationResponse } from '../../../models/designation.model';

export interface DesignationFormDialogData {
  designation?: DesignationResponse;
}

@Component({
  selector: 'app-designation-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule
  ],
  templateUrl: './designation-form-dialog.component.html',
  styleUrl: './designation-form-dialog.component.scss',
})
export class DesignationFormDialogComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  form: FormGroup;
  isEditMode: boolean;
  departments: DepartmentResponse[] = [];

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<DesignationFormDialogComponent>,
    private departmentService: DepartmentService,
    @Inject(MAT_DIALOG_DATA) public data: DesignationFormDialogData
  ) {
    this.isEditMode = !!data.designation;

    this.form = this.fb.group({
      title: [data.designation?.title || '', [Validators.required, Validators.maxLength(100)]],
      code: [data.designation?.code || '', [Validators.required, Validators.maxLength(20)]],
      level: [data.designation?.level ?? null, [Validators.required, Validators.min(1)]],
      grade: [data.designation?.grade || '', [Validators.required, Validators.maxLength(20)]],
      departmentId: [data.designation?.departmentId ?? null, [Validators.required]],
      description: [data.designation?.description || '', [Validators.maxLength(500)]]
    });
  }

  ngOnInit(): void {
    this.loadDepartments();
  }

  private loadDepartments(): void {
    this.departmentService.getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (departments) => this.departments = departments
      });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const request: DesignationRequest = {
      title: this.form.value.title,
      code: this.form.value.code,
      level: this.form.value.level,
      grade: this.form.value.grade,
      departmentId: this.form.value.departmentId,
      description: this.form.value.description
    };

    this.dialogRef.close(request);
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
