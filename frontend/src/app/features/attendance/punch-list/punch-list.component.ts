import { Component, OnInit, ViewChild, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable } from 'rxjs';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AttendancePunchService } from '../../../services/attendance-punch.service';
import { NotificationService } from '../../../services/notification.service';
import { AttendancePunchResponse } from '../../../models/attendance-punch.model';
import { Page } from '../../../models/page.model';

@Component({
  selector: 'app-punch-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatCardModule,
    MatChipsModule,
    MatTooltipModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './punch-list.component.html',
  styleUrl: './punch-list.component.scss',
})
export class PunchListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'employeeCode', 'employeeName', 'deviceName', 'punchTime', 'direction', 'source', 'normalized'
  ];
  dataSource = new MatTableDataSource<AttendancePunchResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  employeeId: number | null = null;
  dateFrom: Date | null = null;
  dateTo: Date | null = null;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private punchService: AttendancePunchService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadPunches();
  }

  loadPunches(): void {
    this.loading = true;
    const hasDateRange = this.dateFrom && this.dateTo;
    const hasEmployee = this.employeeId;

    let source$: Observable<Page<AttendancePunchResponse>>;

    if (hasEmployee && hasDateRange) {
      source$ = this.punchService.getByEmployeeAndDateRange(
        this.employeeId!, this.formatDate(this.dateFrom!), this.formatDate(this.dateTo!),
        this.pageIndex, this.pageSize
      );
    } else if (hasEmployee) {
      source$ = this.punchService.getByEmployee(this.employeeId!, this.pageIndex, this.pageSize);
    } else if (hasDateRange) {
      source$ = this.punchService.getByDateRange(
        this.formatDate(this.dateFrom!), this.formatDate(this.dateTo!),
        this.pageIndex, this.pageSize
      );
    } else {
      source$ = this.punchService.getAll(this.pageIndex, this.pageSize);
    }

    source$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          this.dataSource.data = page.content;
          this.totalElements = page.totalElements;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load punch records');
          this.loading = false;
        }
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadPunches();
  }

  onSearch(): void {
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadPunches();
  }

  clearFilters(): void {
    this.employeeId = null;
    this.dateFrom = null;
    this.dateTo = null;
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadPunches();
  }

  getDirectionIcon(direction: string): string {
    return direction === 'IN' ? 'login' : 'logout';
  }

  getDirectionClass(direction: string): string {
    return direction === 'IN' ? 'direction-in' : 'direction-out';
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
