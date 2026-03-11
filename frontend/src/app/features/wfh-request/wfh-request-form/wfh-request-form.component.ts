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
import { WfhRequestService } from '../../../services/wfh-request.service';
import { NotificationService } from '../../../services/notification.service';
import { WfhRequestCreateRequest } from '../../../models/wfh-request.model';

@Component({
  selector: 'app-wfh-request-form',
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
  templateUrl: './wfh-request-form.component.html',
  styleUrl: './wfh-request-form.component.scss',
})
export class WfhRequestFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  recordForm!: FormGroup;
  isEditMode = false;
  recordId: number | null = null;
  loading = false;
  saving = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private wfhRequestService: WfhRequestService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.recordId = +id;
      this.loadRecord(this.recordId);
    }
  }

  private initForm(): void {
    this.recordForm = this.fb.group({
      employeeId: [null, [Validators.required]],
      requestDate: ['', Validators.required],
      reason: ['', [Validators.required, Validators.maxLength(500)]],
      remarks: ['', Validators.maxLength(500)]
    });
  }

  loadRecord(id: number): void {
    this.loading = true;
    this.wfhRequestService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: record => {
          this.recordForm.patchValue({
            employeeId: record.employeeId,
            requestDate: record.requestDate,
            reason: record.reason,
            remarks: record.remarks
          });
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load WFH request');
          this.loading = false;
          this.router.navigate(['/wfh-requests']);
        }
      });
  }

  onSubmit(): void {
    if (this.recordForm.invalid) {
      this.recordForm.markAllAsTouched();
      this.notificationService.error('Please fill in all required fields correctly');
      return;
    }

    this.saving = true;
    const formValue = this.recordForm.value;
    const request: WfhRequestCreateRequest = {
      employeeId: formValue.employeeId,
      requestDate: formValue.requestDate,
      reason: formValue.reason,
      remarks: formValue.remarks || ''
    };

    const operation$ = this.isEditMode && this.recordId
      ? this.wfhRequestService.update(this.recordId, request)
      : this.wfhRequestService.create(request);

    operation$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          const message = this.isEditMode
            ? 'WFH request updated successfully'
            : 'WFH request created successfully';
          this.notificationService.success(message);
          this.saving = false;
          this.router.navigate(['/wfh-requests']);
        },
        error: () => {
          const message = this.isEditMode
            ? 'Failed to update WFH request'
            : 'Failed to create WFH request';
          this.notificationService.error(message);
          this.saving = false;
        }
      });
  }

  onCancel(): void {
    this.router.navigate(['/wfh-requests']);
  }
}
