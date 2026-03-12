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
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { EmployeeSalaryService } from '../../../services/employee-salary.service';
import { SalaryStructureService } from '../../../services/salary-structure.service';
import { NotificationService } from '../../../services/notification.service';
import { EmployeeSalaryDetailRequest } from '../../../models/employee-salary.model';
import { SalaryStructureResponse, SalaryStructureComponentResponse } from '../../../models/salary-structure.model';

interface CtcBreakdownItem {
  componentName: string;
  componentType: string;
  computationType: string;
  monthlyAmount: number;
  annualAmount: number;
}

@Component({
  selector: 'app-employee-salary-form',
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
  templateUrl: './employee-salary-form.component.html',
  styleUrl: './employee-salary-form.component.scss',
})
export class EmployeeSalaryFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  salaryForm!: FormGroup;
  loading = false;
  saving = false;

  availableStructures: SalaryStructureResponse[] = [];
  selectedStructure: SalaryStructureResponse | null = null;
  ctcBreakdown: CtcBreakdownItem[] = [];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private employeeSalaryService: EmployeeSalaryService,
    private salaryStructureService: SalaryStructureService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadStructures();
  }

  private initForm(): void {
    this.salaryForm = this.fb.group({
      employeeId: [null, Validators.required],
      salaryStructureId: [null, Validators.required],
      ctc: [null, [Validators.required, Validators.min(0)]],
      basicSalary: [null, [Validators.required, Validators.min(0)]],
      effectiveDate: ['', Validators.required],
      notes: ['', Validators.maxLength(500)]
    });
  }

  loadStructures(): void {
    this.salaryStructureService.getActive()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: structures => this.availableStructures = structures,
        error: () => this.notificationService.error('Failed to load salary structures')
      });
  }

  onStructureChange(): void {
    const structureId = this.salaryForm.get('salaryStructureId')?.value;
    if (structureId) {
      this.selectedStructure = this.availableStructures.find(s => s.id === structureId) || null;
      this.calculateBreakdown();
    } else {
      this.selectedStructure = null;
      this.ctcBreakdown = [];
    }
  }

  onBasicSalaryChange(): void {
    this.calculateBreakdown();
  }

  calculateBreakdown(): void {
    if (!this.selectedStructure) {
      this.ctcBreakdown = [];
      return;
    }

    const basicSalary = this.salaryForm.get('basicSalary')?.value || 0;
    this.ctcBreakdown = this.selectedStructure.components.map((comp: SalaryStructureComponentResponse) => {
      let monthlyAmount = 0;

      if (comp.computationType === 'FIXED') {
        monthlyAmount = comp.fixedAmount || 0;
      } else if (comp.computationType === 'PERCENTAGE_OF_BASIC') {
        monthlyAmount = basicSalary * (comp.percentageValue || 0) / 100;
      }

      return {
        componentName: comp.salaryComponentName,
        componentType: comp.salaryComponentType,
        computationType: comp.computationType,
        monthlyAmount,
        annualAmount: monthlyAmount * 12
      };
    });
  }

  getTotalMonthly(): number {
    return this.ctcBreakdown.reduce((sum, item) => sum + item.monthlyAmount, 0);
  }

  getTotalAnnual(): number {
    return this.ctcBreakdown.reduce((sum, item) => sum + item.annualAmount, 0);
  }

  onSubmit(): void {
    if (this.salaryForm.invalid) {
      this.salaryForm.markAllAsTouched();
      this.notificationService.error('Please fill in all required fields correctly');
      return;
    }

    this.saving = true;
    const request: EmployeeSalaryDetailRequest = this.salaryForm.value;

    this.employeeSalaryService.create(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Salary assigned successfully');
          this.saving = false;
          this.router.navigate(['/employee-salary']);
        },
        error: () => {
          this.notificationService.error('Failed to assign salary');
          this.saving = false;
        }
      });
  }

  onCancel(): void {
    this.router.navigate(['/employee-salary']);
  }
}
