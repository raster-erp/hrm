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
import { LeavePolicyService } from '../../../services/leave-policy.service';
import { LeaveTypeService } from '../../../services/leave-type.service';
import { NotificationService } from '../../../services/notification.service';
import { LeavePolicyRequest } from '../../../models/leave-policy.model';
import { LeaveTypeResponse } from '../../../models/leave-type.model';

@Component({
  selector: 'app-leave-policy-form',
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
  templateUrl: './leave-policy-form.component.html',
  styleUrl: './leave-policy-form.component.scss',
})
export class LeavePolicyFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  policyForm!: FormGroup;
  isEditMode = false;
  policyId: number | null = null;
  loading = false;
  saving = false;

  leaveTypes: LeaveTypeResponse[] = [];
  accrualFrequencies = ['MONTHLY', 'QUARTERLY', 'ANNUAL'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private leavePolicyService: LeavePolicyService,
    private leaveTypeService: LeaveTypeService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadLeaveTypes();

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
      leaveTypeId: [null, Validators.required],
      accrualFrequency: ['', Validators.required],
      accrualDays: [null, [Validators.required, Validators.min(0.01)]],
      maxAccumulation: [null],
      carryForwardLimit: [null],
      proRataForNewJoiners: [false],
      minServiceDaysRequired: [null],
      description: ['', Validators.maxLength(500)]
    });
  }

  loadLeaveTypes(): void {
    this.leaveTypeService.getActive()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: types => this.leaveTypes = types,
        error: () => this.notificationService.error('Failed to load leave types')
      });
  }

  loadPolicy(id: number): void {
    this.loading = true;
    this.leavePolicyService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: policy => {
          this.policyForm.patchValue({
            name: policy.name,
            leaveTypeId: policy.leaveTypeId,
            accrualFrequency: policy.accrualFrequency,
            accrualDays: policy.accrualDays,
            maxAccumulation: policy.maxAccumulation,
            carryForwardLimit: policy.carryForwardLimit,
            proRataForNewJoiners: policy.proRataForNewJoiners,
            minServiceDaysRequired: policy.minServiceDaysRequired,
            description: policy.description
          });
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load leave policy');
          this.loading = false;
          this.router.navigate(['/leave-policies']);
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
    const request: LeavePolicyRequest = this.policyForm.value;

    const operation$ = this.isEditMode && this.policyId
      ? this.leavePolicyService.update(this.policyId, request)
      : this.leavePolicyService.create(request);

    operation$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          const message = this.isEditMode ? 'Leave policy updated successfully' : 'Leave policy created successfully';
          this.notificationService.success(message);
          this.saving = false;
          this.router.navigate(['/leave-policies']);
        },
        error: () => {
          const message = this.isEditMode ? 'Failed to update leave policy' : 'Failed to create leave policy';
          this.notificationService.error(message);
          this.saving = false;
        }
      });
  }

  onCancel(): void {
    this.router.navigate(['/leave-policies']);
  }
}
