import { Component, Inject, OnInit, ChangeDetectionStrategy, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { UniformService } from '../../../services/uniform.service';
import { NotificationService } from '../../../services/notification.service';
import { UniformRequest, UniformResponse } from '../../../models/uniform.model';

@Component({
  selector: 'app-uniform-form-dialog',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatDialogModule, MatButtonModule, MatInputModule,
    MatFormFieldModule, MatSelectModule, MatProgressSpinnerModule
  ],
  templateUrl: './uniform-form-dialog.component.html',
  styleUrl: './uniform-form-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UniformFormDialogComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  uniformForm!: FormGroup;
  isEditMode = false;
  saving = false;

  uniformTypes = ['SHIRT', 'PANTS', 'COAT', 'SHOES', 'CAP', 'SCRUBS', 'LAB_COAT', 'OTHER'];
  sizes = ['XS', 'S', 'M', 'L', 'XL', 'XXL', 'XXXL'];

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<UniformFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: UniformResponse | null,
    private uniformService: UniformService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.isEditMode = !!this.data;
    this.uniformForm = this.fb.group({
      name: [this.data?.name || '', [Validators.required, Validators.maxLength(100)]],
      type: [this.data?.type || '', Validators.required],
      size: [this.data?.size || ''],
      description: [this.data?.description || '']
    });
  }

  onSubmit(): void {
    if (this.uniformForm.invalid) {
      this.uniformForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    const request: UniformRequest = this.uniformForm.value;

    const operation = this.isEditMode
      ? this.uniformService.update(this.data!.id, request)
      : this.uniformService.create(request);

    operation
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success(
            this.isEditMode ? 'Uniform updated successfully' : 'Uniform created successfully'
          );
          this.saving = false;
          this.dialogRef.close(true);
        },
        error: () => {
          this.notificationService.error(
            this.isEditMode ? 'Failed to update uniform' : 'Failed to create uniform'
          );
          this.saving = false;
        }
      });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
