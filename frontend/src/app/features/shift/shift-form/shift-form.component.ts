import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ShiftService } from '../../../services/shift.service';
import { NotificationService } from '../../../services/notification.service';
import { ShiftRequest } from '../../../models/shift.model';

@Component({
  selector: 'app-shift-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatCardModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './shift-form.component.html',
  styleUrl: './shift-form.component.scss',
})
export class ShiftFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  shiftForm!: FormGroup;
  isEditMode = false;
  shiftId: number | null = null;
  loading = false;
  saving = false;

  types = ['GENERAL', 'MORNING', 'EVENING', 'NIGHT', 'ROTATIONAL', 'SPLIT'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private shiftService: ShiftService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.shiftId = +id;
      this.loadShift(this.shiftId);
    }
  }

  private initForm(): void {
    this.shiftForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      type: ['', Validators.required],
      startTime: ['', Validators.required],
      endTime: ['', Validators.required],
      breakDurationMinutes: [0, [Validators.min(0)]],
      gracePeriodMinutes: [0, [Validators.min(0)]],
      description: ['', Validators.maxLength(500)]
    });
  }

  loadShift(id: number): void {
    this.loading = true;
    this.shiftService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: shift => {
          this.shiftForm.patchValue({
            name: shift.name,
            type: shift.type,
            startTime: shift.startTime,
            endTime: shift.endTime,
            breakDurationMinutes: shift.breakDurationMinutes,
            gracePeriodMinutes: shift.gracePeriodMinutes,
            description: shift.description
          });
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load shift');
          this.loading = false;
          this.router.navigate(['/shifts']);
        }
      });
  }

  onSubmit(): void {
    if (this.shiftForm.invalid) {
      this.shiftForm.markAllAsTouched();
      this.notificationService.error('Please fill in all required fields correctly');
      return;
    }

    this.saving = true;
    const request: ShiftRequest = this.shiftForm.value;

    const operation$ = this.isEditMode && this.shiftId
      ? this.shiftService.update(this.shiftId, request)
      : this.shiftService.create(request);

    operation$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          const message = this.isEditMode ? 'Shift updated successfully' : 'Shift created successfully';
          this.notificationService.success(message);
          this.saving = false;
          this.router.navigate(['/shifts']);
        },
        error: () => {
          const message = this.isEditMode ? 'Failed to update shift' : 'Failed to create shift';
          this.notificationService.error(message);
          this.saving = false;
        }
      });
  }

  onCancel(): void {
    this.router.navigate(['/shifts']);
  }
}
