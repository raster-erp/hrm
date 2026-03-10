import { Component, OnInit, ViewChild, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ShiftService } from '../../../services/shift.service';
import { NotificationService } from '../../../services/notification.service';
import { ShiftResponse } from '../../../models/shift.model';

@Component({
  selector: 'app-shift-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule,
    MatCardModule,
    MatChipsModule,
    MatTooltipModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './shift-list.component.html',
  styleUrl: './shift-list.component.scss',
})
export class ShiftListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'name', 'type', 'startTime', 'endTime', 'breakDurationMinutes', 'gracePeriodMinutes', 'active', 'actions'
  ];
  dataSource = new MatTableDataSource<ShiftResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  selectedType = '';
  types = ['GENERAL', 'MORNING', 'EVENING', 'NIGHT', 'ROTATIONAL', 'SPLIT'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private shiftService: ShiftService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadShifts();
  }

  loadShifts(): void {
    this.loading = true;

    if (this.selectedType) {
      this.shiftService.getByType(this.selectedType)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: shifts => {
            this.totalElements = shifts.length;
            this.dataSource.data = shifts.slice(
              this.pageIndex * this.pageSize,
              (this.pageIndex + 1) * this.pageSize
            );
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load shifts');
            this.loading = false;
          }
        });
    } else {
      this.shiftService.getAll(this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load shifts');
            this.loading = false;
          }
        });
    }
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadShifts();
  }

  onSearch(): void {
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadShifts();
  }

  clearFilters(): void {
    this.selectedType = '';
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadShifts();
  }

  editShift(shift: ShiftResponse): void {
    this.router.navigate(['/shifts', shift.id, 'edit']);
  }

  toggleActive(shift: ShiftResponse): void {
    this.shiftService.updateActive(shift.id, !shift.active)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success(`Shift ${shift.active ? 'deactivated' : 'activated'} successfully`);
          this.loadShifts();
        },
        error: () => this.notificationService.error('Failed to update shift status')
      });
  }

  deleteShift(shift: ShiftResponse): void {
    if (confirm(`Are you sure you want to delete shift "${shift.name}"?`)) {
      this.shiftService.delete(shift.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Shift deleted successfully');
            this.loadShifts();
          },
          error: () => this.notificationService.error('Failed to delete shift')
        });
    }
  }
}
