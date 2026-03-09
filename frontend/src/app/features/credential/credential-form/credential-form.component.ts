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
import { CredentialService } from '../../../services/credential.service';
import { EmployeeService } from '../../../services/employee.service';
import { NotificationService } from '../../../services/notification.service';
import { CredentialRequest } from '../../../models/credential.model';
import { EmployeeResponse } from '../../../models/employee.model';

@Component({
  selector: 'app-credential-form',
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
  templateUrl: './credential-form.component.html',
  styleUrl: './credential-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CredentialFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  credentialForm!: FormGroup;
  isEditMode = false;
  credentialId: number | null = null;
  loading = false;
  saving = false;

  employees: EmployeeResponse[] = [];

  credentialTypes = ['LICENSE', 'CERTIFICATION', 'DEGREE', 'DIPLOMA', 'TRAINING', 'OTHER'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private credentialService: CredentialService,
    private employeeService: EmployeeService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadEmployees();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.credentialId = +id;
      this.loadCredential(this.credentialId);
    }
  }

  private initForm(): void {
    this.credentialForm = this.fb.group({
      employeeId: [null, Validators.required],
      credentialType: ['', Validators.required],
      credentialName: ['', [Validators.required, Validators.maxLength(200)]],
      issuingAuthority: ['', [Validators.required, Validators.maxLength(200)]],
      issueDate: ['', Validators.required],
      expiryDate: [''],
      credentialNumber: [''],
      notes: ['']
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

  loadCredential(id: number): void {
    this.loading = true;
    this.credentialService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: credential => {
          this.credentialForm.patchValue({
            employeeId: credential.employeeId,
            credentialType: credential.credentialType,
            credentialName: credential.credentialName,
            issuingAuthority: credential.issuingAuthority,
            issueDate: credential.issueDate,
            expiryDate: credential.expiryDate || '',
            credentialNumber: credential.credentialNumber || '',
            notes: credential.notes || ''
          });
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load credential');
          this.loading = false;
          this.router.navigate(['/credentials']);
        }
      });
  }

  onSubmit(): void {
    if (this.credentialForm.invalid) {
      this.credentialForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    const formValue = this.credentialForm.value;

    const request: CredentialRequest = {
      employeeId: formValue.employeeId,
      credentialType: formValue.credentialType,
      credentialName: formValue.credentialName,
      issuingAuthority: formValue.issuingAuthority,
      issueDate: this.formatDate(formValue.issueDate),
      expiryDate: formValue.expiryDate ? this.formatDate(formValue.expiryDate) : undefined,
      credentialNumber: formValue.credentialNumber || undefined,
      notes: formValue.notes || undefined
    };

    const operation = this.isEditMode
      ? this.credentialService.update(this.credentialId!, request)
      : this.credentialService.create(request);

    operation
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success(
            this.isEditMode ? 'Credential updated successfully' : 'Credential created successfully'
          );
          this.saving = false;
          this.router.navigate(['/credentials']);
        },
        error: () => {
          this.notificationService.error(
            this.isEditMode ? 'Failed to update credential' : 'Failed to create credential'
          );
          this.saving = false;
        }
      });
  }

  cancel(): void {
    this.router.navigate(['/credentials']);
  }

  private formatDate(date: string | Date): string {
    if (typeof date === 'string') return date;
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
