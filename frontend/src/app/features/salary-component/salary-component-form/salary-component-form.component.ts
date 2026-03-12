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
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { SalaryComponentService } from '../../../services/salary-component.service';
import { NotificationService } from '../../../services/notification.service';
import { SalaryComponentRequest } from '../../../models/salary-component.model';

@Component({
  selector: 'app-salary-component-form',
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
    MatCheckboxModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './salary-component-form.component.html',
  styleUrl: './salary-component-form.component.scss',
})
export class SalaryComponentFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  componentForm!: FormGroup;
  isEditMode = false;
  componentId: number | null = null;
  loading = false;
  saving = false;

  types = ['EARNING', 'DEDUCTION'];
  computationTypes = ['FIXED', 'PERCENTAGE_OF_BASIC', 'SLAB_BASED'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private salaryComponentService: SalaryComponentService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.componentId = +id;
      this.loadComponent(this.componentId);
    }
  }

  private initForm(): void {
    this.componentForm = this.fb.group({
      code: ['', [Validators.required, Validators.maxLength(20)]],
      name: ['', [Validators.required, Validators.maxLength(100)]],
      type: ['', Validators.required],
      computationType: ['', Validators.required],
      percentageValue: [null],
      taxable: [true],
      mandatory: [false],
      description: ['', Validators.maxLength(500)]
    });
  }

  loadComponent(id: number): void {
    this.loading = true;
    this.salaryComponentService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: component => {
          this.componentForm.patchValue({
            code: component.code,
            name: component.name,
            type: component.type,
            computationType: component.computationType,
            percentageValue: component.percentageValue,
            taxable: component.taxable,
            mandatory: component.mandatory,
            description: component.description
          });
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load salary component');
          this.loading = false;
          this.router.navigate(['/salary-components']);
        }
      });
  }

  onSubmit(): void {
    if (this.componentForm.invalid) {
      this.componentForm.markAllAsTouched();
      this.notificationService.error('Please fill in all required fields correctly');
      return;
    }

    this.saving = true;
    const request: SalaryComponentRequest = this.componentForm.value;

    const operation$ = this.isEditMode && this.componentId
      ? this.salaryComponentService.update(this.componentId, request)
      : this.salaryComponentService.create(request);

    operation$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          const message = this.isEditMode ? 'Component updated successfully' : 'Component created successfully';
          this.notificationService.success(message);
          this.saving = false;
          this.router.navigate(['/salary-components']);
        },
        error: () => {
          const message = this.isEditMode ? 'Failed to update component' : 'Failed to create component';
          this.notificationService.error(message);
          this.saving = false;
        }
      });
  }

  onCancel(): void {
    this.router.navigate(['/salary-components']);
  }
}
