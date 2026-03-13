import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { SalaryStructureService } from '../../../services/salary-structure.service';
import { SalaryComponentService } from '../../../services/salary-component.service';
import { NotificationService } from '../../../services/notification.service';
import { SalaryStructureRequest, SalaryStructureComponentRequest } from '../../../models/salary-structure.model';
import { SalaryComponentResponse } from '../../../models/salary-component.model';

@Component({
  selector: 'app-salary-structure-form',
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
  templateUrl: './salary-structure-form.component.html',
  styleUrl: './salary-structure-form.component.scss',
})
export class SalaryStructureFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  structureForm!: FormGroup;
  isEditMode = false;
  structureId: number | null = null;
  loading = false;
  saving = false;

  availableComponents: SalaryComponentResponse[] = [];
  computationTypes = ['FIXED', 'PERCENTAGE_OF_BASIC', 'SLAB_BASED'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private salaryStructureService: SalaryStructureService,
    private salaryComponentService: SalaryComponentService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadAvailableComponents();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.structureId = +id;
      this.loadStructure(this.structureId);
    }
  }

  private initForm(): void {
    this.structureForm = this.fb.group({
      code: ['', [Validators.required, Validators.maxLength(20)]],
      name: ['', [Validators.required, Validators.maxLength(100)]],
      description: ['', Validators.maxLength(500)],
      components: this.fb.array([])
    });
  }

  get componentsArray(): FormArray {
    return this.structureForm.get('components') as FormArray;
  }

  loadAvailableComponents(): void {
    this.salaryComponentService.getActive()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: components => this.availableComponents = components,
        error: () => this.notificationService.error('Failed to load salary components')
      });
  }

  loadStructure(id: number): void {
    this.loading = true;
    this.salaryStructureService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: structure => {
          this.structureForm.patchValue({
            code: structure.code,
            name: structure.name,
            description: structure.description
          });

          this.componentsArray.clear();
          for (const comp of structure.components) {
            this.componentsArray.push(this.fb.group({
              salaryComponentId: [comp.salaryComponentId, Validators.required],
              computationType: [comp.computationType, Validators.required],
              percentageValue: [comp.percentageValue],
              fixedAmount: [comp.fixedAmount],
              sortOrder: [comp.sortOrder]
            }));
          }
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load salary structure');
          this.loading = false;
          this.router.navigate(['/salary-structures']);
        }
      });
  }

  addComponent(): void {
    this.componentsArray.push(this.fb.group({
      salaryComponentId: [null, Validators.required],
      computationType: ['FIXED', Validators.required],
      percentageValue: [null],
      fixedAmount: [null],
      sortOrder: [this.componentsArray.length]
    }));
  }

  removeComponent(index: number): void {
    this.componentsArray.removeAt(index);
  }

  getComponentName(componentId: number): string {
    const found = this.availableComponents.find(c => c.id === componentId);
    return found ? found.name : '';
  }

  onSubmit(): void {
    if (this.structureForm.invalid) {
      this.structureForm.markAllAsTouched();
      this.notificationService.error('Please fill in all required fields correctly');
      return;
    }

    this.saving = true;
    const formValue = this.structureForm.value;
    const request: SalaryStructureRequest = {
      code: formValue.code,
      name: formValue.name,
      description: formValue.description,
      components: formValue.components as SalaryStructureComponentRequest[]
    };

    const operation$ = this.isEditMode && this.structureId
      ? this.salaryStructureService.update(this.structureId, request)
      : this.salaryStructureService.create(request);

    operation$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          const message = this.isEditMode ? 'Structure updated successfully' : 'Structure created successfully';
          this.notificationService.success(message);
          this.saving = false;
          this.router.navigate(['/salary-structures']);
        },
        error: () => {
          const message = this.isEditMode ? 'Failed to update structure' : 'Failed to create structure';
          this.notificationService.error(message);
          this.saving = false;
        }
      });
  }

  onCancel(): void {
    this.router.navigate(['/salary-structures']);
  }
}
