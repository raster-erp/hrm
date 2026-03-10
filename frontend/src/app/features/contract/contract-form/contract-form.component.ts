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
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ContractService } from '../../../services/contract.service';
import { EmployeeService } from '../../../services/employee.service';
import { NotificationService } from '../../../services/notification.service';
import { ContractRequest } from '../../../models/contract.model';
import { EmployeeResponse } from '../../../models/employee.model';

@Component({
  selector: 'app-contract-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
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
  templateUrl: './contract-form.component.html',
  styleUrl: './contract-form.component.scss',
})
export class ContractFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  contractForm!: FormGroup;
  isEditMode = false;
  contractId: number | null = null;
  loading = false;
  saving = false;

  employees: EmployeeResponse[] = [];
  contractTypes = ['PERMANENT', 'PROBATION', 'FIXED_TERM', 'CONSULTANT'];
  statuses = ['ACTIVE', 'EXPIRED', 'RENEWED', 'TERMINATED'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private contractService: ContractService,
    private employeeService: EmployeeService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadEmployees();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.contractId = +id;
      this.loadContract(this.contractId);
    }
  }

  private initForm(): void {
    this.contractForm = this.fb.group({
      employeeId: [null as number | null, Validators.required],
      contractType: ['', Validators.required],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      terms: [''],
      status: ['ACTIVE', Validators.required]
    });
  }

  loadEmployees(): void {
    this.employeeService.getAll(0, 1000)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => this.employees = page.content,
        error: () => this.notificationService.error('Failed to load employees')
      });
  }

  loadContract(id: number): void {
    this.loading = true;
    this.contractService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: contract => {
          this.contractForm.patchValue({
            employeeId: contract.employeeId,
            contractType: contract.contractType,
            startDate: contract.startDate,
            endDate: contract.endDate,
            terms: contract.terms,
            status: contract.status
          });
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load contract');
          this.loading = false;
          this.router.navigate(['/contracts']);
        }
      });
  }

  onSubmit(): void {
    if (this.contractForm.invalid) {
      this.contractForm.markAllAsTouched();
      this.notificationService.error('Please fill in all required fields correctly');
      return;
    }

    this.saving = true;
    const request = this.buildRequest();

    const operation$ = this.isEditMode && this.contractId
      ? this.contractService.update(this.contractId, request)
      : this.contractService.create(request);

    operation$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          const message = this.isEditMode ? 'Contract updated successfully' : 'Contract created successfully';
          this.notificationService.success(message);
          this.saving = false;
          this.router.navigate(['/contracts']);
        },
        error: () => {
          const message = this.isEditMode ? 'Failed to update contract' : 'Failed to create contract';
          this.notificationService.error(message);
          this.saving = false;
        }
      });
  }

  onCancel(): void {
    this.router.navigate(['/contracts']);
  }

  formatType(type: string): string {
    return type.replace(/_/g, ' ');
  }

  private buildRequest(): ContractRequest {
    const formValue = this.contractForm.value;
    return {
      employeeId: formValue.employeeId,
      contractType: formValue.contractType,
      startDate: this.formatDateValue(formValue.startDate),
      endDate: this.formatDateValue(formValue.endDate),
      terms: formValue.terms,
      status: formValue.status
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
}
