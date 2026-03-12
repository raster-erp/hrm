import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { LeaveCalendarService } from '../../../services/leave-calendar.service';
import { NotificationService } from '../../../services/notification.service';
import { DepartmentService } from '../../../services/department.service';
import { TeamAvailabilityResponse } from '../../../models/leave-calendar.model';
import { DepartmentResponse } from '../../../models/department.model';

@Component({
  selector: 'app-team-availability',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatTableModule
  ],
  templateUrl: './team-availability.component.html',
  styleUrl: './team-availability.component.scss',
})
export class TeamAvailabilityComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  loading = false;
  departments: DepartmentResponse[] = [];
  selectedDepartmentId: number | null = null;
  startDate = '';
  endDate = '';

  displayedColumns: string[] = [
    'date', 'totalMembers', 'availableMembers', 'onLeave',
    'onPlannedLeave', 'coveragePercentage', 'absentEmployees'
  ];
  dataSource = new MatTableDataSource<TeamAvailabilityResponse>();

  constructor(
    private leaveCalendarService: LeaveCalendarService,
    private departmentService: DepartmentService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadDepartments();
    this.setDefaultDates();
  }

  private loadDepartments(): void {
    this.departmentService.getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: departments => this.departments = departments,
        error: () => this.notificationService.error('Failed to load departments')
      });
  }

  private setDefaultDates(): void {
    const today = new Date();
    const endOfMonth = new Date(today.getFullYear(), today.getMonth() + 1, 0);
    this.startDate = this.formatDate(today);
    this.endDate = this.formatDate(endOfMonth);
  }

  loadAvailability(): void {
    if (!this.selectedDepartmentId || !this.startDate || !this.endDate) {
      this.notificationService.error('Please select a department and date range');
      return;
    }

    this.loading = true;
    this.leaveCalendarService.getTeamAvailability(
      this.selectedDepartmentId,
      this.startDate,
      this.endDate
    )
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: data => {
          this.dataSource.data = data;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load team availability');
          this.loading = false;
        }
      });
  }

  getCoverageClass(percentage: number): string {
    if (percentage >= 80) return 'coverage-good';
    if (percentage >= 50) return 'coverage-warning';
    return 'coverage-critical';
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
