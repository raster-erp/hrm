import { Component, OnInit, ChangeDetectionStrategy, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
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
import { TransferService } from '../../../services/transfer.service';
import { EmployeeService } from '../../../services/employee.service';
import { DepartmentService } from '../../../services/department.service';
import { NotificationService } from '../../../services/notification.service';
import { TransferRequest } from '../../../models/transfer.model';
import { EmployeeResponse } from '../../../models/employee.model';
import { DepartmentResponse } from '../../../models/department.model';

@Component({
  selector: 'app-transfer-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatButtonModule, MatIconModule, MatInputModule,
    MatFormFieldModule, MatSelectModule, MatDatepickerModule,
    MatNativeDateModule, MatCardModule, MatProgressSpinnerModule
  ],
  templateUrl: './transfer-form.component.html',
  styleUrl: './transfer-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransferFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  transferForm!: FormGroup;
  saving = false;
  employees: EmployeeResponse[] = [];
  departments: DepartmentResponse[] = [];
  transferTypes = ['INTER_DEPARTMENT', 'INTER_BRANCH', 'INTER_COMPANY'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private transferService: TransferService,
    private employeeService: EmployeeService,
    private departmentService: DepartmentService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadData();
  }

  private initForm(): void {
    this.transferForm = this.fb.group({
      employeeId: [null, Validators.required],
      fromDepartmentId: [null, Validators.required],
      toDepartmentId: [null, Validators.required],
      fromBranch: [''],
      toBranch: [''],
      transferType: ['', Validators.required],
      effectiveDate: ['', Validators.required],
      reason: ['']
    });
  }

  loadData(): void {
    this.employeeService.getAll(0, 1000)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(page => this.employees = page.content);

    this.departmentService.getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(departments => this.departments = departments);
  }

  onEmployeeSelected(): void {
    const empId = this.transferForm.get('employeeId')?.value;
    const employee = this.employees.find(e => e.id === empId);
    if (employee?.departmentId) {
      this.transferForm.patchValue({ fromDepartmentId: employee.departmentId });
    }
  }

  onSubmit(): void {
    if (this.transferForm.invalid) {
      this.transferForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    const formValue = this.transferForm.value;

    const request: TransferRequest = {
      employeeId: formValue.employeeId,
      fromDepartmentId: formValue.fromDepartmentId,
      toDepartmentId: formValue.toDepartmentId,
      fromBranch: formValue.fromBranch || undefined,
      toBranch: formValue.toBranch || undefined,
      transferType: formValue.transferType,
      effectiveDate: this.formatDate(formValue.effectiveDate),
      reason: formValue.reason || undefined
    };

    this.transferService.create(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Transfer request created successfully');
          this.saving = false;
          this.router.navigate(['/transfers']);
        },
        error: () => {
          this.notificationService.error('Failed to create transfer request');
          this.saving = false;
        }
      });
  }

  cancel(): void {
    this.router.navigate(['/transfers']);
  }

  private formatDate(date: string | Date): string {
    if (typeof date === 'string') return date;
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
