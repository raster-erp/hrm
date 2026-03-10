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
import { MatStepperModule } from '@angular/material/stepper';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatListModule } from '@angular/material/list';
import { ShiftRosterService } from '../../../services/shift-roster.service';
import { ShiftService } from '../../../services/shift.service';
import { RotationPatternService } from '../../../services/rotation-pattern.service';
import { EmployeeService } from '../../../services/employee.service';
import { DepartmentService } from '../../../services/department.service';
import { NotificationService } from '../../../services/notification.service';
import { BulkShiftRosterRequest } from '../../../models/shift-roster.model';
import { ShiftResponse } from '../../../models/shift.model';
import { RotationPatternResponse } from '../../../models/rotation-pattern.model';
import { EmployeeResponse } from '../../../models/employee.model';
import { DepartmentResponse } from '../../../models/department.model';

@Component({
  selector: 'app-bulk-assign-wizard',
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
    MatProgressSpinnerModule,
    MatStepperModule,
    MatCheckboxModule,
    MatListModule
  ],
  templateUrl: './bulk-assign-wizard.component.html',
  styleUrl: './bulk-assign-wizard.component.scss'
})
export class BulkAssignWizardComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  shiftForm!: FormGroup;
  loading = false;
  submitting = false;

  departments: DepartmentResponse[] = [];
  employees: EmployeeResponse[] = [];
  shifts: ShiftResponse[] = [];
  rotationPatterns: RotationPatternResponse[] = [];

  selectedDepartmentId: number | null = null;
  selectedEmployeeIds = new Set<number>();

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private shiftRosterService: ShiftRosterService,
    private shiftService: ShiftService,
    private rotationPatternService: RotationPatternService,
    private employeeService: EmployeeService,
    private departmentService: DepartmentService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadData();
  }

  private initForm(): void {
    this.shiftForm = this.fb.group({
      shiftId: [null, Validators.required],
      effectiveDate: ['', Validators.required],
      endDate: [''],
      rotationPatternId: [null]
    });
  }

  private loadData(): void {
    this.loading = true;

    this.departmentService.getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: departments => this.departments = departments,
        error: () => this.notificationService.error('Failed to load departments')
      });

    this.employeeService.getAll(0, 200)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          this.employees = page.content;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load employees');
          this.loading = false;
        }
      });

    this.shiftService.getActive()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: shifts => this.shifts = shifts,
        error: () => this.notificationService.error('Failed to load shifts')
      });

    this.rotationPatternService.getAll(0, 100)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => this.rotationPatterns = page.content,
        error: () => this.notificationService.error('Failed to load rotation patterns')
      });
  }

  get filteredEmployees(): EmployeeResponse[] {
    if (this.selectedDepartmentId === null) {
      return this.employees;
    }
    return this.employees.filter(e => e.departmentId === this.selectedDepartmentId);
  }

  get isEmployeeStepValid(): boolean {
    return this.selectedEmployeeIds.size > 0;
  }

  get selectedShiftName(): string {
    const shift = this.shifts.find(s => s.id === this.shiftForm.value.shiftId);
    return shift ? shift.name : '';
  }

  get selectedRotationPatternName(): string {
    const pattern = this.rotationPatterns.find(p => p.id === this.shiftForm.value.rotationPatternId);
    return pattern ? pattern.name : '';
  }

  get selectedEmployees(): EmployeeResponse[] {
    return this.employees.filter(e => this.selectedEmployeeIds.has(e.id));
  }

  onDepartmentFilterChange(departmentId: number | null): void {
    this.selectedDepartmentId = departmentId;
  }

  toggleEmployee(employeeId: number): void {
    if (this.selectedEmployeeIds.has(employeeId)) {
      this.selectedEmployeeIds.delete(employeeId);
    } else {
      this.selectedEmployeeIds.add(employeeId);
    }
  }

  isEmployeeSelected(employeeId: number): boolean {
    return this.selectedEmployeeIds.has(employeeId);
  }

  selectAll(): void {
    this.filteredEmployees.forEach(e => this.selectedEmployeeIds.add(e.id));
  }

  deselectAll(): void {
    this.filteredEmployees.forEach(e => this.selectedEmployeeIds.delete(e.id));
  }

  onSubmit(): void {
    if (this.shiftForm.invalid || this.selectedEmployeeIds.size === 0) {
      return;
    }

    this.submitting = true;
    const formValue = this.shiftForm.value;
    const request: BulkShiftRosterRequest = {
      employeeIds: Array.from(this.selectedEmployeeIds),
      shiftId: formValue.shiftId,
      effectiveDate: formValue.effectiveDate,
      endDate: formValue.endDate || null,
      rotationPatternId: formValue.rotationPatternId || null
    };

    this.shiftRosterService.bulkCreate(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: results => {
          this.notificationService.success(`Successfully created ${results.length} roster assignments`);
          this.submitting = false;
          this.router.navigate(['/shift-rosters']);
        },
        error: () => {
          this.notificationService.error('Failed to create bulk roster assignments');
          this.submitting = false;
        }
      });
  }

  onCancel(): void {
    this.router.navigate(['/shift-rosters']);
  }
}
