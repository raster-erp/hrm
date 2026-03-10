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
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { ShiftRosterService } from '../../../services/shift-roster.service';
import { ShiftService } from '../../../services/shift.service';
import { RotationPatternService } from '../../../services/rotation-pattern.service';
import { NotificationService } from '../../../services/notification.service';
import { ShiftRosterRequest } from '../../../models/shift-roster.model';
import { ShiftResponse } from '../../../models/shift.model';
import { RotationPatternResponse } from '../../../models/rotation-pattern.model';

@Component({
  selector: 'app-shift-roster-form',
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
    MatDatepickerModule,
    MatNativeDateModule
  ],
  templateUrl: './shift-roster-form.component.html',
  styleUrl: './shift-roster-form.component.scss',
})
export class ShiftRosterFormComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  rosterForm!: FormGroup;
  isEditMode = false;
  rosterId: number | null = null;
  loading = false;
  saving = false;

  shifts: ShiftResponse[] = [];
  rotationPatterns: RotationPatternResponse[] = [];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private shiftRosterService: ShiftRosterService,
    private shiftService: ShiftService,
    private rotationPatternService: RotationPatternService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadDropdownData();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.rosterId = +id;
      this.loadRoster(this.rosterId);
    }
  }

  private initForm(): void {
    this.rosterForm = this.fb.group({
      employeeId: [null, Validators.required],
      shiftId: [null, Validators.required],
      effectiveDate: ['', Validators.required],
      endDate: [''],
      rotationPatternId: [null]
    });
  }

  private loadDropdownData(): void {
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

  loadRoster(id: number): void {
    this.loading = true;
    this.shiftRosterService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: roster => {
          this.rosterForm.patchValue({
            employeeId: roster.employeeId,
            shiftId: roster.shiftId,
            effectiveDate: roster.effectiveDate,
            endDate: roster.endDate,
            rotationPatternId: roster.rotationPatternId
          });
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load roster assignment');
          this.loading = false;
          this.router.navigate(['/shift-rosters']);
        }
      });
  }

  onSubmit(): void {
    if (this.rosterForm.invalid) {
      this.rosterForm.markAllAsTouched();
      this.notificationService.error('Please fill in all required fields correctly');
      return;
    }

    this.saving = true;
    const formValue = this.rosterForm.value;
    const request: ShiftRosterRequest = {
      employeeId: formValue.employeeId,
      shiftId: formValue.shiftId,
      effectiveDate: formValue.effectiveDate,
      endDate: formValue.endDate || null,
      rotationPatternId: formValue.rotationPatternId || null
    };

    const operation$ = this.isEditMode && this.rosterId
      ? this.shiftRosterService.update(this.rosterId, request)
      : this.shiftRosterService.create(request);

    operation$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          const message = this.isEditMode ? 'Roster assignment updated successfully' : 'Roster assignment created successfully';
          this.notificationService.success(message);
          this.saving = false;
          this.router.navigate(['/shift-rosters']);
        },
        error: () => {
          const message = this.isEditMode ? 'Failed to update roster assignment' : 'Failed to create roster assignment';
          this.notificationService.error(message);
          this.saving = false;
        }
      });
  }

  onCancel(): void {
    this.router.navigate(['/shift-rosters']);
  }
}
