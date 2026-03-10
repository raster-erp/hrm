import { Component, OnInit, ViewChild, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ShiftRosterService } from '../../../services/shift-roster.service';
import { NotificationService } from '../../../services/notification.service';
import { ShiftRosterResponse } from '../../../models/shift-roster.model';

@Component({
  selector: 'app-shift-roster-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatTooltipModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './shift-roster-list.component.html',
  styleUrl: './shift-roster-list.component.scss',
})
export class ShiftRosterListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'employeeCode', 'employeeName', 'shiftName', 'effectiveDate', 'endDate', 'rotationPatternName', 'actions'
  ];
  dataSource = new MatTableDataSource<ShiftRosterResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private shiftRosterService: ShiftRosterService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadRosters();
  }

  loadRosters(): void {
    this.loading = true;
    this.shiftRosterService.getAll(this.pageIndex, this.pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          this.dataSource.data = page.content;
          this.totalElements = page.totalElements;
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load shift rosters');
          this.loading = false;
        }
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadRosters();
  }

  editRoster(roster: ShiftRosterResponse): void {
    this.router.navigate(['/shift-rosters', roster.id, 'edit']);
  }

  deleteRoster(roster: ShiftRosterResponse): void {
    if (confirm(`Are you sure you want to delete roster assignment for "${roster.employeeName}"?`)) {
      this.shiftRosterService.delete(roster.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Roster assignment deleted successfully');
            this.loadRosters();
          },
          error: () => this.notificationService.error('Failed to delete roster assignment')
        });
    }
  }
}
