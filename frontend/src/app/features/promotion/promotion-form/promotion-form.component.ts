import { Component, OnInit, ChangeDetectionStrategy, DestroyRef, inject } from '@angular/core';
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
import { PromotionService } from '../../../services/promotion.service';
import { EmployeeService } from '../../../services/employee.service';
import { DesignationService } from '../../../services/designation.service';
import { NotificationService } from '../../../services/notification.service';
import { PromotionRequest } from '../../../models/promotion.model';
import { EmployeeResponse } from '../../../models/employee.model';
import { DesignationResponse } from '../../../models/designation.model';

@Component({
  selector: 'app-promotion-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatButtonModule, MatIconModule, MatInputModule,
    MatFormFieldModule, MatSelectModule, MatDatepickerModule,
    MatNativeDateModule, MatCardModule, MatProgressSpinnerModule
  ],
  templateUrl: './promotion-form.component.html',
  styleUrl: './promotion-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PromotionFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  promotionForm!: FormGroup;
  saving = false;
  employees: EmployeeResponse[] = [];
  designations: DesignationResponse[] = [];

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private promotionService: PromotionService,
    private employeeService: EmployeeService,
    private designationService: DesignationService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadData();
  }

  private initForm(): void {
    this.promotionForm = this.fb.group({
      employeeId: [null, Validators.required],
      oldDesignationId: [null, Validators.required],
      newDesignationId: [null, Validators.required],
      oldGrade: [''],
      newGrade: [''],
      effectiveDate: ['', Validators.required],
      reason: ['']
    });
  }

  loadData(): void {
    this.employeeService.getAll(0, 1000)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(page => this.employees = page.content);

    this.designationService.getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(designations => this.designations = designations);
  }

  onEmployeeSelected(): void {
    const empId = this.promotionForm.get('employeeId')?.value;
    const employee = this.employees.find(e => e.id === empId);
    if (employee?.designationId) {
      this.promotionForm.patchValue({ oldDesignationId: employee.designationId });
      const designation = this.designations.find(d => d.id === employee.designationId);
      if (designation?.grade) {
        this.promotionForm.patchValue({ oldGrade: designation.grade });
      }
    }
  }

  onSubmit(): void {
    if (this.promotionForm.invalid) {
      this.promotionForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    const formValue = this.promotionForm.value;

    const request: PromotionRequest = {
      employeeId: formValue.employeeId,
      oldDesignationId: formValue.oldDesignationId,
      newDesignationId: formValue.newDesignationId,
      oldGrade: formValue.oldGrade || undefined,
      newGrade: formValue.newGrade || undefined,
      effectiveDate: this.formatDate(formValue.effectiveDate),
      reason: formValue.reason || undefined
    };

    this.promotionService.create(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Promotion request created successfully');
          this.saving = false;
          this.router.navigate(['/promotions']);
        },
        error: () => {
          this.notificationService.error('Failed to create promotion request');
          this.saving = false;
        }
      });
  }

  cancel(): void {
    this.router.navigate(['/promotions']);
  }

  private formatDate(date: string | Date): string {
    if (typeof date === 'string') return date;
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
