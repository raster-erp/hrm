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
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { OvertimePolicyService } from '../../../services/overtime-policy.service';
import { NotificationService } from '../../../services/notification.service';
import { OvertimePolicyRequest } from '../../../models/overtime-policy.model';

@Component({
  selector: 'app-overtime-policy-form',
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
    MatCheckboxModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './overtime-policy-form.component.html',
  styleUrl: './overtime-policy-form.component.scss',
})
export class OvertimePolicyFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  policyForm!: FormGroup;
  isEditMode = false;
  policyId: number | null = null;
  loading = false;
  saving = false;

  types = ['WEEKDAY', 'WEEKEND', 'HOLIDAY', 'DOUBLE_TIME'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private overtimePolicyService: OvertimePolicyService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.policyId = +id;
      this.loadPolicy(this.policyId);
    }
  }

  private initForm(): void {
    this.policyForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      type: ['', Validators.required],
      rateMultiplier: [null, [Validators.required, Validators.min(0.01)]],
      minOvertimeMinutes: [null, [Validators.min(0)]],
      maxOvertimeMinutesPerDay: [null],
      maxOvertimeMinutesPerMonth: [null],
      requiresApproval: [true],
      description: ['', Validators.maxLength(500)]
    });
  }

  loadPolicy(id: number): void {
    this.loading = true;
    this.overtimePolicyService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: policy => {
          this.policyForm.patchValue({
            name: policy.name,
            type: policy.type,
            rateMultiplier: policy.rateMultiplier,
            minOvertimeMinutes: policy.minOvertimeMinutes,
            maxOvertimeMinutesPerDay: policy.maxOvertimeMinutesPerDay,
            maxOvertimeMinutesPerMonth: policy.maxOvertimeMinutesPerMonth,
            requiresApproval: policy.requiresApproval,
            description: policy.description
          });
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load overtime policy');
          this.loading = false;
          this.router.navigate(['/overtime-policies']);
        }
      });
  }

  onSubmit(): void {
    if (this.policyForm.invalid) {
      this.policyForm.markAllAsTouched();
      this.notificationService.error('Please fill in all required fields correctly');
      return;
    }

    this.saving = true;
    const request: OvertimePolicyRequest = this.policyForm.value;

    const operation$ = this.isEditMode && this.policyId
      ? this.overtimePolicyService.update(this.policyId, request)
      : this.overtimePolicyService.create(request);

    operation$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          const message = this.isEditMode ? 'Overtime policy updated successfully' : 'Overtime policy created successfully';
          this.notificationService.success(message);
          this.saving = false;
          this.router.navigate(['/overtime-policies']);
        },
        error: () => {
          const message = this.isEditMode ? 'Failed to update overtime policy' : 'Failed to create overtime policy';
          this.notificationService.error(message);
          this.saving = false;
        }
      });
  }

  onCancel(): void {
    this.router.navigate(['/overtime-policies']);
  }
}
