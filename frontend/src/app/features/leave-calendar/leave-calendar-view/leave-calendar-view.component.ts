import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { LeaveCalendarService } from '../../../services/leave-calendar.service';
import { NotificationService } from '../../../services/notification.service';
import { DepartmentService } from '../../../services/department.service';
import { LeaveCalendarEntry } from '../../../models/leave-calendar.model';
import { DepartmentResponse } from '../../../models/department.model';

interface CalendarDay {
  date: Date;
  dayOfMonth: number;
  isCurrentMonth: boolean;
  isToday: boolean;
  entries: LeaveCalendarEntry[];
}

@Component({
  selector: 'app-leave-calendar-view',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatChipsModule
  ],
  templateUrl: './leave-calendar-view.component.html',
  styleUrl: './leave-calendar-view.component.scss',
})
export class LeaveCalendarViewComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  loading = false;
  currentDate = new Date();
  calendarDays: CalendarDay[] = [];
  weekDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  entries: LeaveCalendarEntry[] = [];
  departments: DepartmentResponse[] = [];

  selectedDepartmentId: number | null = null;
  selectedEmployeeId: number | null = null;

  constructor(
    private leaveCalendarService: LeaveCalendarService,
    private departmentService: DepartmentService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadDepartments();
    this.loadCalendar();
  }

  private loadDepartments(): void {
    this.departmentService.getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: departments => this.departments = departments,
        error: () => this.notificationService.error('Failed to load departments')
      });
  }

  loadCalendar(): void {
    this.loading = true;
    const start = this.getMonthStart();
    const end = this.getMonthEnd();

    this.leaveCalendarService.getCalendarEntries(
      this.formatDate(start),
      this.formatDate(end),
      this.selectedEmployeeId ?? undefined,
      this.selectedDepartmentId ?? undefined
    )
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: entries => {
          this.entries = entries;
          this.buildCalendar();
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load calendar entries');
          this.loading = false;
        }
      });
  }

  previousMonth(): void {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() - 1, 1);
    this.loadCalendar();
  }

  nextMonth(): void {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, 1);
    this.loadCalendar();
  }

  goToToday(): void {
    this.currentDate = new Date();
    this.loadCalendar();
  }

  onFilterChange(): void {
    this.loadCalendar();
  }

  clearFilters(): void {
    this.selectedDepartmentId = null;
    this.selectedEmployeeId = null;
    this.loadCalendar();
  }

  get currentMonthLabel(): string {
    return this.currentDate.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
  }

  getEntryTooltip(entry: LeaveCalendarEntry): string {
    if (entry.type === 'HOLIDAY') {
      return `Holiday: ${entry.leaveTypeName}`;
    }
    return `${entry.employeeName}: ${entry.leaveTypeName} (${entry.status})`;
  }

  private buildCalendar(): void {
    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const today = new Date();

    const days: CalendarDay[] = [];

    // Fill days from previous month
    const startOffset = firstDay.getDay();
    for (let i = startOffset - 1; i >= 0; i--) {
      const date = new Date(year, month, -i);
      days.push({
        date,
        dayOfMonth: date.getDate(),
        isCurrentMonth: false,
        isToday: false,
        entries: this.getEntriesForDate(date)
      });
    }

    // Fill current month days
    for (let d = 1; d <= lastDay.getDate(); d++) {
      const date = new Date(year, month, d);
      days.push({
        date,
        dayOfMonth: d,
        isCurrentMonth: true,
        isToday: date.toDateString() === today.toDateString(),
        entries: this.getEntriesForDate(date)
      });
    }

    // Fill remaining days to complete the grid
    const remaining = 42 - days.length;
    for (let i = 1; i <= remaining; i++) {
      const date = new Date(year, month + 1, i);
      days.push({
        date,
        dayOfMonth: i,
        isCurrentMonth: false,
        isToday: false,
        entries: this.getEntriesForDate(date)
      });
    }

    this.calendarDays = days;
  }

  private getEntriesForDate(date: Date): LeaveCalendarEntry[] {
    const dateStr = this.formatDate(date);
    return this.entries.filter(entry => {
      return dateStr >= entry.fromDate && dateStr <= entry.toDate;
    });
  }

  private getMonthStart(): Date {
    return new Date(this.currentDate.getFullYear(), this.currentDate.getMonth(), 1);
  }

  private getMonthEnd(): Date {
    return new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, 0);
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
