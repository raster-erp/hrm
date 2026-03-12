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
import { CompOffService } from '../../../services/comp-off.service';
import { NotificationService } from '../../../services/notification.service';
import { EmployeeService } from '../../../services/employee.service';
import { EmployeeResponse } from '../../../models/employee.model';

@Component({
  selector: 'app-comp-off-form',
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
  templateUrl: './comp-off-form.component.html',
  styleUrl: './comp-off-form.component.scss',
})
export class CompOffFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  form!: FormGroup;
  submitting = false;
  employees: EmployeeResponse[] = [];

  constructor(
    private fb: FormBuilder,
    private compOffService: CompOffService,
    private employeeService: EmployeeService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadEmployees();
  }

  private initForm(): void {
    this.form = this.fb.group({
      employeeId: [null, [Validators.required]],
      workedDate: [null, [Validators.required]],
      reason: ['', [Validators.required, Validators.maxLength(255)]],
      hoursWorked: [null, [Validators.min(0), Validators.max(24)]],
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

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    const formValue = this.form.getRawValue();

    const workedDate = formValue.workedDate instanceof Date
      ? formValue.workedDate.toISOString().split('T')[0]
      : formValue.workedDate;

    this.compOffService.create({
      employeeId: formValue.employeeId,
      workedDate: workedDate,
      reason: formValue.reason,
      hoursWorked: formValue.hoursWorked || undefined,
      remarks: formValue.remarks || undefined
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Comp-off request submitted successfully');
          this.router.navigate(['/comp-off']);
          this.submitting = false;
        },
        error: () => {
          this.notificationService.error('Failed to submit comp-off request');
          this.submitting = false;
        }
      });
  }
}
