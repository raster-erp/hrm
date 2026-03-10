import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatStepperModule } from '@angular/material/stepper';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { EmployeeService } from '../../../services/employee.service';
import { DepartmentService } from '../../../services/department.service';
import { DesignationService } from '../../../services/designation.service';
import { NotificationService } from '../../../services/notification.service';
import { EmployeeRequest } from '../../../models/employee.model';
import { DepartmentResponse } from '../../../models/department.model';
import { DesignationResponse } from '../../../models/designation.model';

@Component({
  selector: 'app-employee-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatStepperModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatCardModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './employee-form.component.html',
  styleUrl: './employee-form.component.scss',
})
export class EmployeeFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  personalInfoForm!: FormGroup;
  contactInfoForm!: FormGroup;
  emergencyContactForm!: FormGroup;
  bankDetailsForm!: FormGroup;
  employmentInfoForm!: FormGroup;

  isEditMode = false;
  employeeId: number | null = null;
  loading = false;
  saving = false;

  departments: DepartmentResponse[] = [];
  designations: DesignationResponse[] = [];
  filteredDesignations: DesignationResponse[] = [];

  genders = ['MALE', 'FEMALE', 'OTHER'];
  statuses = ['ACTIVE', 'INACTIVE', 'ON_LEAVE', 'TERMINATED'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private employeeService: EmployeeService,
    private departmentService: DepartmentService,
    private designationService: DesignationService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForms();
    this.loadDepartments();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.employeeId = +id;
      this.loadEmployee(this.employeeId);
    }
  }

  private initForms(): void {
    this.personalInfoForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.maxLength(100)]],
      lastName: ['', [Validators.required, Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern(/^\+?[\d\s-]{7,15}$/)]],
      dateOfBirth: ['', Validators.required],
      gender: ['', Validators.required]
    });

    this.contactInfoForm = this.fb.group({
      address: ['', [Validators.required, Validators.maxLength(255)]],
      city: ['', [Validators.required, Validators.maxLength(100)]],
      state: ['', [Validators.required, Validators.maxLength(100)]],
      country: ['', [Validators.required, Validators.maxLength(100)]],
      zipCode: ['', [Validators.required, Validators.maxLength(20)]]
    });

    this.emergencyContactForm = this.fb.group({
      emergencyContactName: ['', [Validators.required, Validators.maxLength(200)]],
      emergencyContactPhone: ['', [Validators.required, Validators.pattern(/^\+?[\d\s-]{7,15}$/)]]
    });

    this.bankDetailsForm = this.fb.group({
      bankName: ['', [Validators.required, Validators.maxLength(100)]],
      bankAccountNumber: ['', [Validators.required, Validators.maxLength(50)]],
      bankIfscCode: ['', [Validators.required, Validators.maxLength(20)]]
    });

    this.employmentInfoForm = this.fb.group({
      employeeCode: ['', [Validators.required, Validators.maxLength(20)]],
      departmentId: [null as number | null, Validators.required],
      designationId: [null as number | null, Validators.required],
      joiningDate: ['', Validators.required],
      employmentStatus: ['ACTIVE', Validators.required]
    });
  }

  loadDepartments(): void {
    this.departmentService.getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: departments => this.departments = departments,
        error: () => this.notificationService.error('Failed to load departments')
      });
  }

  onDepartmentChange(departmentId: number): void {
    this.employmentInfoForm.get('designationId')?.setValue(null);
    this.filteredDesignations = [];

    if (departmentId) {
      this.designationService.getByDepartment(departmentId)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: designations => this.filteredDesignations = designations,
          error: () => this.notificationService.error('Failed to load designations')
        });
    }
  }

  loadEmployee(id: number): void {
    this.loading = true;
    this.employeeService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: employee => {
          this.personalInfoForm.patchValue({
            firstName: employee.firstName,
            lastName: employee.lastName,
            email: employee.email,
            phone: employee.phone,
            dateOfBirth: employee.dateOfBirth,
            gender: employee.gender
          });

          this.contactInfoForm.patchValue({
            address: employee.address,
            city: employee.city,
            state: employee.state,
            country: employee.country,
            zipCode: employee.zipCode
          });

          this.emergencyContactForm.patchValue({
            emergencyContactName: employee.emergencyContactName,
            emergencyContactPhone: employee.emergencyContactPhone
          });

          this.bankDetailsForm.patchValue({
            bankName: employee.bankName,
            bankAccountNumber: employee.bankAccountNumber,
            bankIfscCode: employee.bankIfscCode
          });

          this.employmentInfoForm.patchValue({
            employeeCode: employee.employeeCode,
            departmentId: employee.departmentId,
            designationId: employee.designationId,
            joiningDate: employee.joiningDate,
            employmentStatus: employee.employmentStatus
          });

          // Load designations for the employee's department
          if (employee.departmentId) {
            this.designationService.getByDepartment(employee.departmentId)
              .pipe(takeUntilDestroyed(this.destroyRef))
              .subscribe({
                next: designations => this.filteredDesignations = designations
              });
          }

          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load employee');
          this.loading = false;
          this.router.navigate(['/employees']);
        }
      });
  }

  onSubmit(): void {
    if (this.personalInfoForm.invalid || this.contactInfoForm.invalid ||
        this.emergencyContactForm.invalid || this.bankDetailsForm.invalid ||
        this.employmentInfoForm.invalid) {
      this.markAllAsTouched();
      this.notificationService.error('Please fill in all required fields correctly');
      return;
    }

    this.saving = true;
    const request = this.buildRequest();

    const operation$ = this.isEditMode && this.employeeId
      ? this.employeeService.update(this.employeeId, request)
      : this.employeeService.create(request);

    operation$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          const message = this.isEditMode ? 'Employee updated successfully' : 'Employee created successfully';
          this.notificationService.success(message);
          this.saving = false;
          this.router.navigate(['/employees']);
        },
        error: () => {
          const message = this.isEditMode ? 'Failed to update employee' : 'Failed to create employee';
          this.notificationService.error(message);
          this.saving = false;
        }
      });
  }

  onCancel(): void {
    this.router.navigate(['/employees']);
  }

  private buildRequest(): EmployeeRequest {
    const dob = this.personalInfoForm.get('dateOfBirth')?.value;
    const joiningDate = this.employmentInfoForm.get('joiningDate')?.value;

    return {
      ...this.personalInfoForm.value,
      ...this.contactInfoForm.value,
      ...this.emergencyContactForm.value,
      ...this.bankDetailsForm.value,
      ...this.employmentInfoForm.value,
      dateOfBirth: this.formatDateValue(dob),
      joiningDate: this.formatDateValue(joiningDate)
    };
  }

  private formatDateValue(value: unknown): string {
    if (value instanceof Date) {
      const year = value.getFullYear();
      const month = String(value.getMonth() + 1).padStart(2, '0');
      const day = String(value.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    }
    return String(value ?? '');
  }

  private markAllAsTouched(): void {
    this.personalInfoForm.markAllAsTouched();
    this.contactInfoForm.markAllAsTouched();
    this.emergencyContactForm.markAllAsTouched();
    this.bankDetailsForm.markAllAsTouched();
    this.employmentInfoForm.markAllAsTouched();
  }
}
