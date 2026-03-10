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
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { SeparationService } from '../../../services/separation.service';
import { EmployeeService } from '../../../services/employee.service';
import { NotificationService } from '../../../services/notification.service';
import { EmployeeResponse } from '../../../models/employee.model';

@Component({
  selector: 'app-separation-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatButtonModule, MatIconModule, MatInputModule,
    MatFormFieldModule, MatSelectModule, MatDatepickerModule,
    MatNativeDateModule, MatCardModule, MatProgressSpinnerModule
  ],
  templateUrl: './separation-form.component.html',
  styleUrl: './separation-form.component.scss',
})
export class SeparationFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  separationForm!: FormGroup;
  saving = false;
  employees: EmployeeResponse[] = [];
  separationTypes = ['RESIGNATION', 'TERMINATION', 'RETIREMENT', 'END_OF_CONTRACT', 'DEATH', 'ABSCONDING'];

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private separationService: SeparationService,
    private employeeService: EmployeeService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadEmployees();
  }

  private initForm(): void {
    this.separationForm = this.fb.group({
      employeeId: [null, Validators.required],
      separationType: ['', Validators.required],
      reason: ['', Validators.required],
      noticeDate: ['', Validators.required],
      lastWorkingDay: ['', Validators.required]
    });
  }

  private loadEmployees(): void {
    this.employeeService.getAll(0, 1000)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(page => this.employees = page.content);
  }

  onSubmit(): void {
    if (this.separationForm.invalid) return;
    this.saving = true;

    const formValue = this.separationForm.value;
    const request = {
      ...formValue,
      noticeDate: this.formatDate(formValue.noticeDate),
      lastWorkingDay: this.formatDate(formValue.lastWorkingDay)
    };

    this.separationService.create(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: result => {
          this.notificationService.success('Separation record created');
          this.router.navigate(['/separations', result.id]);
        },
        error: () => {
          this.notificationService.error('Failed to create separation record');
          this.saving = false;
        }
      });
  }

  private formatDate(date: Date | string): string {
    if (date instanceof Date) {
      return date.toISOString().split('T')[0];
    }
    return date;
  }

  cancel(): void {
    this.router.navigate(['/separations']);
  }
}
