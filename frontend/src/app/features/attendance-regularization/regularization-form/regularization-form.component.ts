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
import { RegularizationRequestService } from '../../../services/regularization-request.service';
import { NotificationService } from '../../../services/notification.service';
import { RegularizationRequestRequest } from '../../../models/regularization-request.model';

@Component({
  selector: 'app-regularization-form',
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
  templateUrl: './regularization-form.component.html',
  styleUrl: './regularization-form.component.scss',
})
export class RegularizationFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  form!: FormGroup;
  isEdit = false;
  recordId: number | null = null;
  loading = false;
  submitting = false;

  types = ['MISSED_PUNCH', 'ON_DUTY', 'CLIENT_VISIT'];

  constructor(
    private fb: FormBuilder,
    private regularizationService: RegularizationRequestService,
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
      requestDate: ['', [Validators.required]],
      type: ['', [Validators.required]],
      reason: ['', [Validators.required, Validators.maxLength(500)]],
      originalPunchIn: [''],
      originalPunchOut: [''],
      correctedPunchIn: ['', [Validators.required]],
      correctedPunchOut: ['', [Validators.required]],
      remarks: ['']
    });
  }

  private loadRecord(id: number): void {
    this.loading = true;
    this.regularizationService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: record => {
          if (record.status !== 'PENDING') {
            this.form.disable();
          }
          this.form.patchValue({
            employeeId: record.employeeId,
            requestDate: record.requestDate,
            type: record.type,
            reason: record.reason,
            originalPunchIn: record.originalPunchIn ?? '',
            originalPunchOut: record.originalPunchOut ?? '',
            correctedPunchIn: record.correctedPunchIn,
            correctedPunchOut: record.correctedPunchOut,
            remarks: record.remarks ?? ''
          });
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load regularization request');
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

    const request: RegularizationRequestRequest = {
      employeeId: formValue.employeeId,
      requestDate: formValue.requestDate,
      type: formValue.type,
      reason: formValue.reason,
      originalPunchIn: formValue.originalPunchIn || null,
      originalPunchOut: formValue.originalPunchOut || null,
      correctedPunchIn: formValue.correctedPunchIn,
      correctedPunchOut: formValue.correctedPunchOut,
      remarks: formValue.remarks || null
    };

    if (this.isEdit && this.recordId) {
      this.regularizationService.update(this.recordId, request)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Regularization request updated successfully');
            this.router.navigate(['/attendance-regularization']);
            this.submitting = false;
          },
          error: () => {
            this.notificationService.error('Failed to update regularization request');
            this.submitting = false;
          }
        });
    } else {
      this.regularizationService.create(request)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Regularization request submitted successfully');
            this.router.navigate(['/attendance-regularization']);
            this.submitting = false;
          },
          error: () => {
            this.notificationService.error('Failed to submit regularization request');
            this.submitting = false;
          }
        });
    }
  }

  formatType(type: string): string {
    switch (type) {
      case 'MISSED_PUNCH': return 'Missed Punch';
      case 'ON_DUTY': return 'On Duty';
      case 'CLIENT_VISIT': return 'Client Visit';
      default: return type;
    }
  }
}
