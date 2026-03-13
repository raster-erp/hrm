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
import { InvestmentDeclarationService } from '../../../services/investment-declaration.service';
import { NotificationService } from '../../../services/notification.service';
import { InvestmentDeclarationRequest } from '../../../models/investment-declaration.model';

@Component({
  selector: 'app-investment-declaration-form',
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
  templateUrl: './investment-declaration-form.component.html',
  styleUrl: './investment-declaration-form.component.scss',
})
export class InvestmentDeclarationFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  declarationForm!: FormGroup;
  isEditMode = false;
  declarationId: number | null = null;
  loading = false;
  saving = false;

  regimes = ['OLD', 'NEW'];
  sections = ['80C', '80D', '80G', '80E', '24B', 'HRA', 'NPS_80CCD', 'OTHER'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private investmentDeclarationService: InvestmentDeclarationService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.declarationId = +id;
      this.loadDeclaration(this.declarationId);
    }
  }

  private initForm(): void {
    this.declarationForm = this.fb.group({
      employeeId: [null, Validators.required],
      financialYear: ['', Validators.required],
      regime: ['NEW', Validators.required],
      remarks: ['', Validators.maxLength(500)],
      items: this.fb.array([])
    });
  }

  get items(): FormArray {
    return this.declarationForm.get('items') as FormArray;
  }

  addItem(): void {
    this.items.push(this.fb.group({
      section: ['', Validators.required],
      description: ['', Validators.required],
      declaredAmount: [null, Validators.required]
    }));
  }

  removeItem(index: number): void {
    this.items.removeAt(index);
  }

  loadDeclaration(id: number): void {
    this.loading = true;
    this.investmentDeclarationService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: decl => {
          this.declarationForm.patchValue({
            employeeId: decl.employeeId,
            financialYear: decl.financialYear,
            regime: decl.regime,
            remarks: decl.remarks
          });
          this.items.clear();
          for (const item of decl.items) {
            this.items.push(this.fb.group({
              section: [item.section, Validators.required],
              description: [item.description, Validators.required],
              declaredAmount: [item.declaredAmount, Validators.required]
            }));
          }
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load declaration');
          this.loading = false;
          this.router.navigate(['/investment-declarations']);
        }
      });
  }

  onSubmit(): void {
    if (this.declarationForm.invalid) {
      this.declarationForm.markAllAsTouched();
      this.notificationService.error('Please fill in all required fields correctly');
      return;
    }

    this.saving = true;
    const request: InvestmentDeclarationRequest = this.declarationForm.value;

    const operation$ = this.isEditMode && this.declarationId
      ? this.investmentDeclarationService.update(this.declarationId, request)
      : this.investmentDeclarationService.create(request);

    operation$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          const message = this.isEditMode ? 'Declaration updated successfully' : 'Declaration created successfully';
          this.notificationService.success(message);
          this.saving = false;
          this.router.navigate(['/investment-declarations']);
        },
        error: () => {
          const message = this.isEditMode ? 'Failed to update declaration' : 'Failed to create declaration';
          this.notificationService.error(message);
          this.saving = false;
        }
      });
  }

  onCancel(): void {
    this.router.navigate(['/investment-declarations']);
  }
}
