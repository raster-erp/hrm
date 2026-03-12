import { Component, OnInit, ViewChild, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDialogModule } from '@angular/material/dialog';
import { HolidayService } from '../../../services/holiday.service';
import { NotificationService } from '../../../services/notification.service';
import { HolidayResponse, HolidayType } from '../../../models/holiday.model';

@Component({
  selector: 'app-holiday-setup',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCardModule,
    MatChipsModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatDialogModule
  ],
  templateUrl: './holiday-setup.component.html',
  styleUrl: './holiday-setup.component.scss',
})
export class HolidaySetupComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = ['name', 'date', 'type', 'region', 'description', 'active', 'actions'];
  dataSource = new MatTableDataSource<HolidayResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  showForm = false;
  editingId: number | null = null;
  form!: FormGroup;
  submitting = false;

  holidayTypes: HolidayType[] = ['PUBLIC', 'REGIONAL', 'OPTIONAL', 'COMPANY'];
  selectedType = '';

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private fb: FormBuilder,
    private holidayService: HolidayService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadRecords();
  }

  private initForm(): void {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      date: [null, [Validators.required]],
      type: [null, [Validators.required]],
      region: ['', [Validators.maxLength(100)]],
      description: ['', [Validators.maxLength(500)]]
    });
  }

  loadRecords(): void {
    this.loading = true;

    if (this.selectedType) {
      this.holidayService.getByType(this.selectedType, this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load holidays');
            this.loading = false;
          }
        });
    } else {
      this.holidayService.getAll(this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load holidays');
            this.loading = false;
          }
        });
    }
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadRecords();
  }

  onSearch(): void {
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadRecords();
  }

  clearFilters(): void {
    this.selectedType = '';
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadRecords();
  }

  openNewForm(): void {
    this.editingId = null;
    this.form.reset();
    this.showForm = true;
  }

  openEditForm(holiday: HolidayResponse): void {
    this.editingId = holiday.id;
    this.form.patchValue({
      name: holiday.name,
      date: new Date(holiday.date + 'T00:00:00'),
      type: holiday.type,
      region: holiday.region || '',
      description: holiday.description || ''
    });
    this.showForm = true;
  }

  cancelForm(): void {
    this.showForm = false;
    this.editingId = null;
    this.form.reset();
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    const formValue = this.form.getRawValue();

    const dateStr = formValue.date instanceof Date
      ? formValue.date.toISOString().split('T')[0]
      : formValue.date;

    const request = {
      name: formValue.name,
      date: dateStr,
      type: formValue.type,
      region: formValue.region || undefined,
      description: formValue.description || undefined
    };

    const obs = this.editingId
      ? this.holidayService.update(this.editingId, request)
      : this.holidayService.create(request);

    obs.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success(
            this.editingId ? 'Holiday updated successfully' : 'Holiday created successfully'
          );
          this.cancelForm();
          this.loadRecords();
          this.submitting = false;
        },
        error: () => {
          this.notificationService.error(
            this.editingId ? 'Failed to update holiday' : 'Failed to create holiday'
          );
          this.submitting = false;
        }
      });
  }

  deactivateHoliday(id: number): void {
    this.holidayService.deactivate(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Holiday deactivated successfully');
          this.loadRecords();
        },
        error: () => {
          this.notificationService.error('Failed to deactivate holiday');
        }
      });
  }
}
