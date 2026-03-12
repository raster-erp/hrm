import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LeaveEncashmentService } from '../../../services/leave-encashment.service';
import { NotificationService } from '../../../services/notification.service';
import { EmployeeService } from '../../../services/employee.service';
import { LeaveTypeService } from '../../../services/leave-type.service';
import { EncashmentEligibilityResponse } from '../../../models/leave-encashment.model';
import { EmployeeResponse } from '../../../models/employee.model';
import { LeaveTypeResponse } from '../../../models/leave-type.model';

@Component({
  selector: 'app-leave-encashment-form',
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
  templateUrl: './leave-encashment-form.component.html',
  styleUrl: './leave-encashment-form.component.scss',
})
export class LeaveEncashmentFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  form!: FormGroup;
  submitting = false;
  checkingEligibility = false;

  employees: EmployeeResponse[] = [];
  leaveTypes: LeaveTypeResponse[] = [];
  eligibility: EncashmentEligibilityResponse | null = null;
  calculatedAmount = 0;

  constructor(
    private fb: FormBuilder,
    private leaveEncashmentService: LeaveEncashmentService,
    private employeeService: EmployeeService,
    private leaveTypeService: LeaveTypeService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadEmployees();
    this.loadLeaveTypes();
  }

  private initForm(): void {
    this.form = this.fb.group({
      employeeId: [null, [Validators.required]],
      leaveTypeId: [null, [Validators.required]],
      year: [new Date().getFullYear(), [Validators.required]],
      numberOfDays: [{ value: null, disabled: true }, [Validators.required, Validators.min(1)]],
      remarks: ['']
    });
  }

  private loadEmployees(): void {
    this.employeeService.getAll(0, 1000)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => this.employees = page.content,
        error: () => this.notificationService.error('Failed to load employees')
      });
  }

  private loadLeaveTypes(): void {
    this.leaveTypeService.getActive()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: types => this.leaveTypes = types,
        error: () => this.notificationService.error('Failed to load leave types')
      });
  }

  checkEligibility(): void {
    const employeeId = this.form.get('employeeId')?.value;
    const leaveTypeId = this.form.get('leaveTypeId')?.value;
    const year = this.form.get('year')?.value;

    if (!employeeId || !leaveTypeId || !year) {
      this.notificationService.error('Please select employee, leave type, and year');
      return;
    }

    this.checkingEligibility = true;
    this.eligibility = null;
    this.calculatedAmount = 0;
    this.form.get('numberOfDays')?.disable();
    this.form.get('numberOfDays')?.reset();

    this.leaveEncashmentService.checkEligibility(employeeId, leaveTypeId, year)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: result => {
          this.eligibility = result;
          if (result.eligible) {
            this.form.get('numberOfDays')?.enable();
            this.form.get('numberOfDays')?.setValidators([
              Validators.required,
              Validators.min(1),
              Validators.max(result.maxEncashableDays)
            ]);
            this.form.get('numberOfDays')?.updateValueAndValidity();
          }
          this.checkingEligibility = false;
        },
        error: () => {
          this.notificationService.error('Failed to check eligibility');
          this.checkingEligibility = false;
        }
      });
  }

  onDaysChange(): void {
    const days = this.form.get('numberOfDays')?.value;
    if (days && this.eligibility) {
      this.calculatedAmount = days * this.eligibility.perDaySalary;
    } else {
      this.calculatedAmount = 0;
    }
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    const formValue = this.form.getRawValue();

    this.leaveEncashmentService.create({
      employeeId: formValue.employeeId,
      leaveTypeId: formValue.leaveTypeId,
      numberOfDays: formValue.numberOfDays,
      remarks: formValue.remarks || undefined
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Encashment request submitted successfully');
          this.router.navigate(['/leave-encashments']);
          this.submitting = false;
        },
        error: () => {
          this.notificationService.error('Failed to submit encashment request');
          this.submitting = false;
        }
      });
  }
}
