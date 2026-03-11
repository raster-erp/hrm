import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LeaveApplicationService } from '../../../services/leave-application.service';
import { NotificationService } from '../../../services/notification.service';
import { LeaveApplicationRequest } from '../../../models/leave-application.model';

@Component({
  selector: 'app-leave-application-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './leave-application-form.component.html',
  styleUrl: './leave-application-form.component.scss',
})
export class LeaveApplicationFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  form!: FormGroup;
  isEdit = false;
  recordId: number | null = null;
  loading = false;
  submitting = false;

  constructor(
    private fb: FormBuilder,
    private leaveApplicationService: LeaveApplicationService,
    private notificationService: NotificationService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.initForm();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.recordId = +id;
      this.loadRecord(this.recordId);
    }
  }

  private initForm(): void {
    this.form = this.fb.group({
      employeeId: [null, [Validators.required]],
      leaveTypeId: [null, [Validators.required]],
      fromDate: ['', [Validators.required]],
      toDate: ['', [Validators.required]],
      numberOfDays: [null, [Validators.required, Validators.min(0.5)]],
      reason: ['', [Validators.maxLength(500)]],
      remarks: ['']
    });
  }

  private loadRecord(id: number): void {
    this.loading = true;
    this.leaveApplicationService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: record => {
          if (record.status !== 'PENDING') {
            this.form.disable();
          }
          this.form.patchValue({
            employeeId: record.employeeId,
            leaveTypeId: record.leaveTypeId,
            fromDate: record.fromDate,
            toDate: record.toDate,
            numberOfDays: record.numberOfDays,
            reason: record.reason ?? '',
            remarks: record.remarks ?? ''
          });
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load leave application');
          this.loading = false;
        }
      });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    const formValue = this.form.value;

    const request: LeaveApplicationRequest = {
      employeeId: formValue.employeeId,
      leaveTypeId: formValue.leaveTypeId,
      fromDate: formValue.fromDate,
      toDate: formValue.toDate,
      numberOfDays: formValue.numberOfDays,
      reason: formValue.reason || null,
      remarks: formValue.remarks || null
    };

    if (this.isEdit && this.recordId) {
      this.leaveApplicationService.update(this.recordId, request)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Leave application updated successfully');
            this.router.navigate(['/leave-applications']);
            this.submitting = false;
          },
          error: () => {
            this.notificationService.error('Failed to update leave application');
            this.submitting = false;
          }
        });
    } else {
      this.leaveApplicationService.create(request)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Leave application submitted successfully');
            this.router.navigate(['/leave-applications']);
            this.submitting = false;
          },
          error: () => {
            this.notificationService.error('Failed to submit leave application');
            this.submitting = false;
          }
        });
    }
  }
}
