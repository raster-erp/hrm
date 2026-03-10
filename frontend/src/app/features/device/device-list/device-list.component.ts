import { Component, OnInit, ViewChild, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable, map } from 'rxjs';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DeviceService } from '../../../services/device.service';
import { NotificationService } from '../../../services/notification.service';
import { DeviceResponse } from '../../../models/device.model';

@Component({
  selector: 'app-device-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatCardModule,
    MatChipsModule,
    MatTooltipModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './device-list.component.html',
  styleUrl: './device-list.component.scss',
})
export class DeviceListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'serialNumber', 'name', 'type', 'location', 'ipAddress', 'status', 'lastSyncAt', 'actions'
  ];
  dataSource = new MatTableDataSource<DeviceResponse>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;

  selectedStatus = '';
  selectedType = '';

  statuses = ['ACTIVE', 'INACTIVE', 'OFFLINE'];
  types = ['BIOMETRIC', 'RFID'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private deviceService: DeviceService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDevices();
  }

  loadDevices(): void {
    this.loading = true;

    if (this.selectedStatus && !this.selectedType) {
      this.handleListResult(this.deviceService.getByStatus(this.selectedStatus));
    } else if (this.selectedType && !this.selectedStatus) {
      this.handleListResult(this.deviceService.getByType(this.selectedType));
    } else if (this.selectedStatus && this.selectedType) {
      this.handleListResult(
        this.deviceService.getByStatus(this.selectedStatus).pipe(
          map(devices => devices.filter(d => d.type === this.selectedType))
        )
      );
    } else {
      this.deviceService.getAll(this.pageIndex, this.pageSize)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: page => {
            this.dataSource.data = page.content;
            this.totalElements = page.totalElements;
            this.loading = false;
          },
          error: () => {
            this.notificationService.error('Failed to load devices');
            this.loading = false;
          }
        });
    }
  }

  private handleListResult(source$: Observable<DeviceResponse[]>): void {
    source$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: devices => {
          this.totalElements = devices.length;
          this.dataSource.data = devices.slice(
            this.pageIndex * this.pageSize,
            (this.pageIndex + 1) * this.pageSize
          );
          this.loading = false;
        },
        error: () => {
          this.notificationService.error('Failed to load devices');
          this.loading = false;
        }
      });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadDevices();
  }

  onSearch(): void {
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadDevices();
  }

  clearFilters(): void {
    this.selectedStatus = '';
    this.selectedType = '';
    this.pageIndex = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.loadDevices();
  }

  editDevice(device: DeviceResponse): void {
    this.router.navigate(['/devices', device.id, 'edit']);
  }

  syncDevice(device: DeviceResponse): void {
    this.deviceService.recordSync(device.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Device sync recorded successfully');
          this.loadDevices();
        },
        error: () => this.notificationService.error('Failed to sync device')
      });
  }

  deleteDevice(device: DeviceResponse): void {
    if (confirm(`Are you sure you want to delete device "${device.name}" (${device.serialNumber})?`)) {
      this.deviceService.delete(device.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notificationService.success('Device deleted successfully');
            this.loadDevices();
          },
          error: () => this.notificationService.error('Failed to delete device')
        });
    }
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'primary';
      case 'INACTIVE': return 'warn';
      case 'OFFLINE': return 'warn';
      default: return '';
    }
  }

  getTypeIcon(type: string): string {
    switch (type) {
      case 'BIOMETRIC': return 'fingerprint';
      case 'RFID': return 'contactless';
      default: return 'devices';
    }
  }
}
