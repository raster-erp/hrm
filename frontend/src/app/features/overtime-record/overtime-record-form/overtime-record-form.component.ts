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
import { OvertimeRecordService } from '../../../services/overtime-record.service';
import { OvertimePolicyService } from '../../../services/overtime-policy.service';
import { NotificationService } from '../../../services/notification.service';
import { OvertimeRecordRequest } from '../../../models/overtime-record.model';
import { OvertimePolicyResponse } from '../../../models/overtime-policy.model';

@Component({
  selector: 'app-overtime-record-form',
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
  templateUrl: './overtime-record-form.component.html',
  styleUrl: './overtime-record-form.component.scss',
})
export class OvertimeRecordFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  recordForm!: FormGroup;
  isEditMode = false;
  recordId: number | null = null;
  loading = false;
  saving = false;

  activePolicies: OvertimePolicyResponse[] = [];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private overtimeRecordService: OvertimeRecordService,
    private overtimePolicyService: OvertimePolicyService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadActivePolicies();

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
      overtimeDate: ['', Validators.required],
      overtimePolicyId: [null, Validators.required],
      overtimeMinutes: [null, [Validators.required, Validators.min(1)]],
      shiftStartTime: [''],
      shiftEndTime: [''],
      remarks: ['', Validators.maxLength(500)]
    });
  }

  loadActivePolicies(): void {
    this.overtimePolicyService.getActive()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: policies => this.activePolicies = policies,
        error: () => this.notificationService.error('Failed to load overtime policies')
      });
  }

  loadRecord(id: number): void {
    this.loading = true;
    this.overtimeRecordService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: record => {
          this.recordForm.patchValue({
            employeeId: record.employeeId,
            overtimeDate: record.overtimeDate,
            overtimePolicyId: record.overtimePolicyId,
            overtimeMinutes: record.overtimeMinutes,
            shiftStartTime: record.shiftStartTime,
            shiftEndTime: record.shiftEndTime,
            remarks: record.remarks
          });
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load overtime record');
          this.loading = false;
          this.router.navigate(['/overtime-records']);
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
    const request: OvertimeRecordRequest = {
      employeeId: formValue.employeeId,
      overtimeDate: formValue.overtimeDate,
      overtimePolicyId: formValue.overtimePolicyId,
      overtimeMinutes: formValue.overtimeMinutes,
      shiftStartTime: formValue.shiftStartTime || null,
      shiftEndTime: formValue.shiftEndTime || null,
      actualStartTime: null,
      actualEndTime: null,
      remarks: formValue.remarks || ''
    };

    const operation$ = this.isEditMode && this.recordId
      ? this.overtimeRecordService.update(this.recordId, request)
      : this.overtimeRecordService.create(request);

    operation$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          const message = this.isEditMode
            ? 'Overtime record updated successfully'
            : 'Overtime record created successfully';
          this.notificationService.success(message);
          this.saving = false;
          this.router.navigate(['/overtime-records']);
        },
        error: () => {
          const message = this.isEditMode
            ? 'Failed to update overtime record'
            : 'Failed to create overtime record';
          this.notificationService.error(message);
          this.saving = false;
        }
      });
  }

  onCancel(): void {
    this.router.navigate(['/overtime-records']);
  }
}
