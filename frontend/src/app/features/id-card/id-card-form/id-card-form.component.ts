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
import { IdCardService } from '../../../services/id-card.service';
import { EmployeeService } from '../../../services/employee.service';
import { NotificationService } from '../../../services/notification.service';
import { IdCardRequest } from '../../../models/id-card.model';
import { EmployeeResponse } from '../../../models/employee.model';

@Component({
  selector: 'app-id-card-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule, MatIconModule, MatInputModule,
    MatFormFieldModule, MatSelectModule, MatDatepickerModule,
    MatNativeDateModule, MatCardModule, MatProgressSpinnerModule
  ],
  templateUrl: './id-card-form.component.html',
  styleUrl: './id-card-form.component.scss',
})
export class IdCardFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  idCardForm!: FormGroup;
  isEditMode = false;
  idCardId: number | null = null;
  loading = false;
  saving = false;
  employees: EmployeeResponse[] = [];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private idCardService: IdCardService,
    private employeeService: EmployeeService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadEmployees();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.idCardId = +id;
      this.loadIdCard(this.idCardId);
    }
  }

  private initForm(): void {
    this.idCardForm = this.fb.group({
      employeeId: [null, Validators.required],
      issueDate: ['', Validators.required],
      expiryDate: ['']
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

  loadIdCard(id: number): void {
    this.loading = true;
    this.idCardService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: idCard => {
          this.idCardForm.patchValue({
            employeeId: idCard.employeeId,
            issueDate: idCard.issueDate,
            expiryDate: idCard.expiryDate || ''
          });
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load ID card');
          this.loading = false;
          this.router.navigate(['/id-cards']);
        }
      });
  }

  onSubmit(): void {
    if (this.idCardForm.invalid) {
      this.idCardForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    const formValue = this.idCardForm.value;

    const request: IdCardRequest = {
      employeeId: formValue.employeeId,
      issueDate: this.formatDate(formValue.issueDate),
      expiryDate: formValue.expiryDate ? this.formatDate(formValue.expiryDate) : ''
    };

    const operation = this.isEditMode
      ? this.idCardService.update(this.idCardId!, request)
      : this.idCardService.create(request);

    operation
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success(
            this.isEditMode ? 'ID card updated successfully' : 'ID card issued successfully'
          );
          this.saving = false;
          this.router.navigate(['/id-cards']);
        },
        error: () => {
          this.notificationService.error(
            this.isEditMode ? 'Failed to update ID card' : 'Failed to issue ID card'
          );
          this.saving = false;
        }
      });
  }

  cancel(): void {
    this.router.navigate(['/id-cards']);
  }

  private formatDate(date: string | Date): string {
    if (typeof date === 'string') return date;
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
