import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ShiftRosterService } from '../../../services/shift-roster.service';
import { EmployeeService } from '../../../services/employee.service';
import { NotificationService } from '../../../services/notification.service';
import { ShiftRosterResponse } from '../../../models/shift-roster.model';
import { EmployeeResponse } from '../../../models/employee.model';

interface CalendarDay {
  date: Date;
  label: string;
  dayOfWeek: string;
  isToday: boolean;
}

interface EmployeeRow {
  employee: EmployeeResponse;
  assignments: Map<string, ShiftRosterResponse>;
}

@Component({
  selector: 'app-roster-calendar',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
  ],
  templateUrl: './roster-calendar.component.html',
  styleUrl: './roster-calendar.component.scss',
})
export class RosterCalendarComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  weekDays: CalendarDay[] = [];
  employeeRows: EmployeeRow[] = [];
  loading = false;
  weekStartDate!: Date;

  constructor(
    private shiftRosterService: ShiftRosterService,
    private employeeService: EmployeeService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.goToToday();
  }

  previousWeek(): void {
    this.weekStartDate = this.addDays(this.weekStartDate, -7);
    this.buildWeek();
    this.loadData();
  }

  nextWeek(): void {
    this.weekStartDate = this.addDays(this.weekStartDate, 7);
    this.buildWeek();
    this.loadData();
  }

  goToToday(): void {
    this.weekStartDate = this.getMonday(new Date());
    this.buildWeek();
    this.loadData();
  }

  getWeekLabel(): string {
    const end = this.addDays(this.weekStartDate, 6);
    const startMonth = this.weekStartDate.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    const endMonth = end.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
    return `${startMonth} – ${endMonth}`;
  }

  getAssignment(row: EmployeeRow, day: CalendarDay): ShiftRosterResponse | undefined {
    return row.assignments.get(this.formatDate(day.date));
  }

  onCellClick(row: EmployeeRow, day: CalendarDay): void {
    const assignment = this.getAssignment(row, day);
    if (assignment) {
      this.router.navigate(['/shift-rosters', assignment.id, 'edit']);
    } else {
      this.router.navigate(['/shift-rosters/new'], {
        queryParams: {
          employeeId: row.employee.id,
          date: this.formatDate(day.date),
        },
      });
    }
  }

  private buildWeek(): void {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const dayNames = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

    this.weekDays = Array.from({ length: 7 }, (_, i) => {
      const date = this.addDays(this.weekStartDate, i);
      return {
        date,
        label: date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
        dayOfWeek: dayNames[i],
        isToday: date.getTime() === today.getTime(),
      };
    });
  }

  private loadData(): void {
    this.loading = true;
    const startDate = this.formatDate(this.weekStartDate);
    const endDate = this.formatDate(this.addDays(this.weekStartDate, 6));

    forkJoin({
      rosters: this.shiftRosterService.getByDateRange(startDate, endDate),
      employees: this.employeeService.getAll(0, 1000),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ rosters, employees }) => {
          this.buildEmployeeRows(employees.content, rosters);
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load roster calendar data');
          this.loading = false;
        },
      });
  }

  private buildEmployeeRows(employees: EmployeeResponse[], rosters: ShiftRosterResponse[]): void {
    this.employeeRows = employees.map((employee) => {
      const assignments = new Map<string, ShiftRosterResponse>();

      rosters
        .filter((r) => r.employeeId === employee.id)
        .forEach((roster) => {
          for (const day of this.weekDays) {
            const dateStr = this.formatDate(day.date);
            if (dateStr >= roster.effectiveDate && (!roster.endDate || dateStr <= roster.endDate)) {
              assignments.set(dateStr, roster);
            }
          }
        });

      return { employee, assignments };
    });
  }

  private getMonday(date: Date): Date {
    const d = new Date(date);
    d.setHours(0, 0, 0, 0);
    const day = d.getDay();
    const diff = d.getDate() - day + (day === 0 ? -6 : 1);
    d.setDate(diff);
    return d;
  }

  private addDays(date: Date, days: number): Date {
    const result = new Date(date);
    result.setDate(result.getDate() + days);
    return result;
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
