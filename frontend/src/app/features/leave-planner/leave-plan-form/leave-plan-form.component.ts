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
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LeavePlanService } from '../../../services/leave-plan.service';
import { NotificationService } from '../../../services/notification.service';
import { EmployeeService } from '../../../services/employee.service';
import { LeaveTypeService } from '../../../services/leave-type.service';
import { EmployeeResponse } from '../../../models/employee.model';
import { LeaveTypeResponse } from '../../../models/leave-type.model';

@Component({
  selector: 'app-leave-plan-form',
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
    MatDatepickerModule,
    MatNativeDateModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './leave-plan-form.component.html',
  styleUrl: './leave-plan-form.component.scss',
})
export class LeavePlanFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  form!: FormGroup;
  submitting = false;
  employees: EmployeeResponse[] = [];
  leaveTypes: LeaveTypeResponse[] = [];

  constructor(
    private fb: FormBuilder,
    private leavePlanService: LeavePlanService,
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
      plannedFromDate: [null, [Validators.required]],
      plannedToDate: [null, [Validators.required]],
      numberOfDays: [null, [Validators.required, Validators.min(0.5)]],
      notes: ['', [Validators.maxLength(500)]]
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
    this.leaveTypeService.getAll(0, 100)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => this.leaveTypes = page.content,
        error: () => this.notificationService.error('Failed to load leave types')
      });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    const formValue = this.form.getRawValue();

    const plannedFromDate = formValue.plannedFromDate instanceof Date
      ? formValue.plannedFromDate.toISOString().split('T')[0]
      : formValue.plannedFromDate;

    const plannedToDate = formValue.plannedToDate instanceof Date
      ? formValue.plannedToDate.toISOString().split('T')[0]
      : formValue.plannedToDate;

    this.leavePlanService.create({
      employeeId: formValue.employeeId,
      leaveTypeId: formValue.leaveTypeId,
      plannedFromDate,
      plannedToDate,
      numberOfDays: formValue.numberOfDays,
      notes: formValue.notes || undefined
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Leave plan created successfully');
          this.router.navigate(['/leave-planner']);
          this.submitting = false;
        },
        error: () => {
          this.notificationService.error('Failed to create leave plan');
          this.submitting = false;
        }
      });
  }
}
