import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RotationPatternService } from '../../../services/rotation-pattern.service';
import { NotificationService } from '../../../services/notification.service';
import { RotationPatternRequest } from '../../../models/rotation-pattern.model';

@Component({
  selector: 'app-rotation-pattern-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatCardModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './rotation-pattern-form.component.html',
  styleUrl: './rotation-pattern-form.component.scss',
})
export class RotationPatternFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  patternForm!: FormGroup;
  isEditMode = false;
  patternId: number | null = null;
  loading = false;
  saving = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private rotationPatternService: RotationPatternService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.patternId = +id;
      this.loadPattern(this.patternId);
    }
  }

  private initForm(): void {
    this.patternForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      description: ['', Validators.maxLength(500)],
      rotationDays: [7, [Validators.required, Validators.min(1)]],
      shiftSequence: ['', [Validators.required, Validators.maxLength(1000)]]
    });
  }

  loadPattern(id: number): void {
    this.loading = true;
    this.rotationPatternService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: pattern => {
          this.patternForm.patchValue({
            name: pattern.name,
            description: pattern.description,
            rotationDays: pattern.rotationDays,
            shiftSequence: pattern.shiftSequence
          });
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load rotation pattern');
          this.loading = false;
          this.router.navigate(['/rotation-patterns']);
        }
      });
  }

  onSubmit(): void {
    if (this.patternForm.invalid) {
      this.patternForm.markAllAsTouched();
      this.notificationService.error('Please fill in all required fields correctly');
      return;
    }

    this.saving = true;
    const request: RotationPatternRequest = this.patternForm.value;

    const operation$ = this.isEditMode && this.patternId
      ? this.rotationPatternService.update(this.patternId, request)
      : this.rotationPatternService.create(request);

    operation$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          const message = this.isEditMode ? 'Rotation pattern updated successfully' : 'Rotation pattern created successfully';
          this.notificationService.success(message);
          this.saving = false;
          this.router.navigate(['/rotation-patterns']);
        },
        error: () => {
          const message = this.isEditMode ? 'Failed to update rotation pattern' : 'Failed to create rotation pattern';
          this.notificationService.error(message);
          this.saving = false;
        }
      });
  }

  onCancel(): void {
    this.router.navigate(['/rotation-patterns']);
  }
}
