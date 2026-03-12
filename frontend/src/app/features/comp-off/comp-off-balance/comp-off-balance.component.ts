import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormControl, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CompOffService } from '../../../services/comp-off.service';
import { NotificationService } from '../../../services/notification.service';
import { EmployeeService } from '../../../services/employee.service';
import { CompOffBalanceResponse } from '../../../models/comp-off.model';
import { EmployeeResponse } from '../../../models/employee.model';

@Component({
  selector: 'app-comp-off-balance',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './comp-off-balance.component.html',
  styleUrl: './comp-off-balance.component.scss',
})
export class CompOffBalanceComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  form!: FormGroup;
  employeeIdControl = new FormControl<number | null>(null, [Validators.required]);
  employees: EmployeeResponse[] = [];
  balance: CompOffBalanceResponse | null = null;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private compOffService: CompOffService,
    private employeeService: EmployeeService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      employeeId: this.employeeIdControl
    });
    this.loadEmployees();
  }

  private loadEmployees(): void {
    this.employeeService.getAll(0, 1000)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => this.employees = page.content,
        error: () => this.notificationService.error('Failed to load employees')
      });
  }

  loadBalance(): void {
    const employeeId = this.employeeIdControl.value;
    if (!employeeId) {
      this.notificationService.error('Please select an employee');
      return;
    }

    this.loading = true;
    this.balance = null;

    this.compOffService.getBalance(employeeId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: result => {
          this.balance = result;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load comp-off balance');
          this.loading = false;
        }
      });
  }
}
