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
import { LeaveTypeService } from '../../../services/leave-type.service';
import { NotificationService } from '../../../services/notification.service';
import { LeaveTypeRequest } from '../../../models/leave-type.model';

@Component({
  selector: 'app-leave-type-form',
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
  templateUrl: './leave-type-form.component.html',
  styleUrl: './leave-type-form.component.scss',
})
export class LeaveTypeFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  leaveTypeForm!: FormGroup;
  isEditMode = false;
  leaveTypeId: number | null = null;
  loading = false;
  saving = false;

  categories = ['PAID', 'UNPAID', 'STATUTORY', 'SPECIAL'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private leaveTypeService: LeaveTypeService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.leaveTypeId = +id;
      this.loadLeaveType(this.leaveTypeId);
    }
  }

  private initForm(): void {
    this.leaveTypeForm = this.fb.group({
      code: ['', [Validators.required, Validators.maxLength(20)]],
      name: ['', [Validators.required, Validators.maxLength(100)]],
      category: ['', Validators.required],
      description: ['', Validators.maxLength(500)]
    });
  }

  loadLeaveType(id: number): void {
    this.loading = true;
    this.leaveTypeService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: leaveType => {
          this.leaveTypeForm.patchValue({
            code: leaveType.code,
            name: leaveType.name,
            category: leaveType.category,
            description: leaveType.description
          });
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load leave type');
          this.loading = false;
          this.router.navigate(['/leave-types']);
        }
      });
  }

  onSubmit(): void {
    if (this.leaveTypeForm.invalid) {
      this.leaveTypeForm.markAllAsTouched();
      this.notificationService.error('Please fill in all required fields correctly');
      return;
    }

    this.saving = true;
    const request: LeaveTypeRequest = this.leaveTypeForm.value;

    const operation$ = this.isEditMode && this.leaveTypeId
      ? this.leaveTypeService.update(this.leaveTypeId, request)
      : this.leaveTypeService.create(request);

    operation$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          const message = this.isEditMode ? 'Leave type updated successfully' : 'Leave type created successfully';
          this.notificationService.success(message);
          this.saving = false;
          this.router.navigate(['/leave-types']);
        },
        error: () => {
          const message = this.isEditMode ? 'Failed to update leave type' : 'Failed to create leave type';
          this.notificationService.error(message);
          this.saving = false;
        }
      });
  }

  onCancel(): void {
    this.router.navigate(['/leave-types']);
  }
}
