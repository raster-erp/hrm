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
import { LeavePolicyAssignmentService } from '../../../services/leave-policy-assignment.service';
import { LeavePolicyService } from '../../../services/leave-policy.service';
import { NotificationService } from '../../../services/notification.service';
import { LeavePolicyAssignmentRequest } from '../../../models/leave-policy-assignment.model';
import { LeavePolicyResponse } from '../../../models/leave-policy.model';

@Component({
  selector: 'app-leave-policy-assignment-form',
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
  templateUrl: './leave-policy-assignment-form.component.html',
  styleUrl: './leave-policy-assignment-form.component.scss',
})
export class LeavePolicyAssignmentFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  assignmentForm!: FormGroup;
  isEditMode = false;
  assignmentId: number | null = null;
  loading = false;
  saving = false;

  leavePolicies: LeavePolicyResponse[] = [];
  assignmentTypes = ['DEPARTMENT', 'DESIGNATION', 'INDIVIDUAL'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private assignmentService: LeavePolicyAssignmentService,
    private leavePolicyService: LeavePolicyService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadLeavePolicies();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.assignmentId = +id;
      this.loadAssignment(this.assignmentId);
    }
  }

  private initForm(): void {
    this.assignmentForm = this.fb.group({
      leavePolicyId: [null, Validators.required],
      assignmentType: ['', Validators.required],
      departmentId: [null],
      designationId: [null],
      employeeId: [null],
      effectiveFrom: ['', Validators.required],
      effectiveTo: ['']
    });
  }

  loadLeavePolicies(): void {
    this.leavePolicyService.getActive()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: policies => this.leavePolicies = policies,
        error: () => this.notificationService.error('Failed to load leave policies')
      });
  }

  loadAssignment(id: number): void {
    this.loading = true;
    this.assignmentService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: assignment => {
          this.assignmentForm.patchValue({
            leavePolicyId: assignment.leavePolicyId,
            assignmentType: assignment.assignmentType,
            departmentId: assignment.departmentId,
            designationId: assignment.designationId,
            employeeId: assignment.employeeId,
            effectiveFrom: assignment.effectiveFrom,
            effectiveTo: assignment.effectiveTo
          });
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load policy assignment');
          this.loading = false;
          this.router.navigate(['/leave-policy-assignments']);
        }
      });
  }

  onSubmit(): void {
    if (this.assignmentForm.invalid) {
      this.assignmentForm.markAllAsTouched();
      this.notificationService.error('Please fill in all required fields correctly');
      return;
    }

    this.saving = true;
    const request: LeavePolicyAssignmentRequest = this.assignmentForm.value;

    const operation$ = this.isEditMode && this.assignmentId
      ? this.assignmentService.update(this.assignmentId, request)
      : this.assignmentService.create(request);

    operation$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          const message = this.isEditMode ? 'Policy assignment updated successfully' : 'Policy assignment created successfully';
          this.notificationService.success(message);
          this.saving = false;
          this.router.navigate(['/leave-policy-assignments']);
        },
        error: () => {
          const message = this.isEditMode ? 'Failed to update policy assignment' : 'Failed to create policy assignment';
          this.notificationService.error(message);
          this.saving = false;
        }
      });
  }

  onCancel(): void {
    this.router.navigate(['/leave-policy-assignments']);
  }
}
