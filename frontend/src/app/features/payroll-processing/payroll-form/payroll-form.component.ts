import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PayrollService } from '../../../services/payroll.service';
import { NotificationService } from '../../../services/notification.service';
import { PayrollRunRequest } from '../../../models/payroll.model';

@Component({
  selector: 'app-payroll-form',
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
  templateUrl: './payroll-form.component.html',
  styleUrl: './payroll-form.component.scss',
})
export class PayrollFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  payrollForm!: FormGroup;
  saving = false;

  readonly months = [
    { value: 1, label: 'January' },
    { value: 2, label: 'February' },
    { value: 3, label: 'March' },
    { value: 4, label: 'April' },
    { value: 5, label: 'May' },
    { value: 6, label: 'June' },
    { value: 7, label: 'July' },
    { value: 8, label: 'August' },
    { value: 9, label: 'September' },
    { value: 10, label: 'October' },
    { value: 11, label: 'November' },
    { value: 12, label: 'December' }
  ];

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private payrollService: PayrollService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    const now = new Date();
    this.payrollForm = this.fb.group({
      periodYear: [now.getFullYear(), [Validators.required, Validators.min(2000), Validators.max(2100)]],
      periodMonth: [now.getMonth() + 1, Validators.required],
      notes: ['', Validators.maxLength(500)]
    });
  }

  onSubmit(): void {
    if (this.payrollForm.invalid) {
      this.payrollForm.markAllAsTouched();
      this.notificationService.error('Please fill in all required fields correctly');
      return;
    }

    this.saving = true;
    const request: PayrollRunRequest = this.payrollForm.value;

    this.payrollService.initializeRun(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Payroll run initialized successfully');
          this.saving = false;
          this.router.navigate(['/payroll-processing']);
        },
        error: () => {
          this.notificationService.error('Failed to initialize payroll run');
          this.saving = false;
        }
      });
  }

  onCancel(): void {
    this.router.navigate(['/payroll-processing']);
  }
}
