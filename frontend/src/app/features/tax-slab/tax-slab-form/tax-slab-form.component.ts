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
import { TaxSlabService } from '../../../services/tax-slab.service';
import { NotificationService } from '../../../services/notification.service';
import { TaxSlabRequest } from '../../../models/tax-slab.model';

@Component({
  selector: 'app-tax-slab-form',
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
  templateUrl: './tax-slab-form.component.html',
  styleUrl: './tax-slab-form.component.scss',
})
export class TaxSlabFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  slabForm!: FormGroup;
  isEditMode = false;
  slabId: number | null = null;
  loading = false;
  saving = false;

  regimes = ['OLD', 'NEW'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private taxSlabService: TaxSlabService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.slabId = +id;
      this.loadSlab(this.slabId);
    }
  }

  private initForm(): void {
    this.slabForm = this.fb.group({
      regime: ['', Validators.required],
      financialYear: ['', Validators.required],
      slabFrom: [null, Validators.required],
      slabTo: [null],
      rate: [null, Validators.required],
      description: ['', Validators.maxLength(500)]
    });
  }

  loadSlab(id: number): void {
    this.loading = true;
    this.taxSlabService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: slab => {
          this.slabForm.patchValue({
            regime: slab.regime,
            financialYear: slab.financialYear,
            slabFrom: slab.slabFrom,
            slabTo: slab.slabTo,
            rate: slab.rate,
            description: slab.description
          });
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load tax slab');
          this.loading = false;
          this.router.navigate(['/tax-slabs']);
        }
      });
  }

  onSubmit(): void {
    if (this.slabForm.invalid) {
      this.slabForm.markAllAsTouched();
      this.notificationService.error('Please fill in all required fields correctly');
      return;
    }

    this.saving = true;
    const request: TaxSlabRequest = this.slabForm.value;

    const operation$ = this.isEditMode && this.slabId
      ? this.taxSlabService.update(this.slabId, request)
      : this.taxSlabService.create(request);

    operation$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          const message = this.isEditMode ? 'Tax slab updated successfully' : 'Tax slab created successfully';
          this.notificationService.success(message);
          this.saving = false;
          this.router.navigate(['/tax-slabs']);
        },
        error: () => {
          const message = this.isEditMode ? 'Failed to update tax slab' : 'Failed to create tax slab';
          this.notificationService.error(message);
          this.saving = false;
        }
      });
  }

  onCancel(): void {
    this.router.navigate(['/tax-slabs']);
  }
}
