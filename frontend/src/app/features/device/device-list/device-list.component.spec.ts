import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { DeviceListComponent } from './device-list.component';
import { DeviceService } from '../../../services/device.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { DeviceResponse } from '../../../models/device.model';

describe('DeviceListComponent', () => {
  let component: DeviceListComponent;
  let fixture: ComponentFixture<DeviceListComponent>;
  let deviceServiceSpy: jasmine.SpyObj<DeviceService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockPage: Page<DeviceResponse> = {
    content: [
      {
        id: 1, serialNumber: 'BIO-001', name: 'Main Entrance',
        type: 'BIOMETRIC', location: 'Lobby', ipAddress: '192.168.1.100',
        status: 'ACTIVE', lastSyncAt: '2026-03-10T10:00:00',
        createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
      }
    ],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    deviceServiceSpy = jasmine.createSpyObj('DeviceService', ['getAll', 'getByStatus', 'getByType', 'recordSync', 'delete']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    deviceServiceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        DeviceListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: DeviceService, useValue: deviceServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: Router, useValue: routerSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({}),
            snapshot: { paramMap: { get: () => null } }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DeviceListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have displayed columns defined', () => {
    expect(component.displayedColumns.length).toBeGreaterThan(0);
  });

  it('should have default page size of 10', () => {
    expect(component.pageSize).toBe(10);
  });

  it('should load devices on init', () => {
    component.ngOnInit();
    expect(deviceServiceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle device load error', () => {
    deviceServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadDevices();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load devices');
  });

  it('should reset page index on search', () => {
    component.pageIndex = 5;
    component.onSearch();
    expect(component.pageIndex).toBe(0);
  });

  it('should update pagination on page change', () => {
    component.onPageChange({ pageIndex: 2, pageSize: 25, length: 100 });
    expect(component.pageIndex).toBe(2);
    expect(component.pageSize).toBe(25);
  });

  it('should clear all filters', () => {
    component.selectedStatus = 'ACTIVE';
    component.selectedType = 'BIOMETRIC';
    component.clearFilters();
    expect(component.selectedStatus).toBe('');
    expect(component.selectedType).toBe('');
  });

  it('should navigate to edit page', () => {
    component.editDevice(mockPage.content[0]);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/devices', 1, 'edit']);
  });

  it('should sync device', () => {
    deviceServiceSpy.recordSync.and.returnValue(of(mockPage.content[0]));
    component.syncDevice(mockPage.content[0]);
    expect(deviceServiceSpy.recordSync).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Device sync recorded successfully');
  });

  it('should handle sync error', () => {
    deviceServiceSpy.recordSync.and.returnValue(throwError(() => new Error('fail')));
    component.syncDevice(mockPage.content[0]);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to sync device');
  });

  it('should delete device after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    deviceServiceSpy.delete.and.returnValue(of(void 0));
    component.deleteDevice(mockPage.content[0]);
    expect(deviceServiceSpy.delete).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalled();
  });

  it('should not delete device if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteDevice(mockPage.content[0]);
    expect(deviceServiceSpy.delete).not.toHaveBeenCalled();
  });

  it('should handle delete error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    deviceServiceSpy.delete.and.returnValue(throwError(() => new Error('fail')));
    component.deleteDevice(mockPage.content[0]);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to delete device');
  });

  it('should return correct status color', () => {
    expect(component.getStatusColor('ACTIVE')).toBe('primary');
    expect(component.getStatusColor('INACTIVE')).toBe('warn');
    expect(component.getStatusColor('OFFLINE')).toBe('warn');
    expect(component.getStatusColor('UNKNOWN')).toBe('');
  });

  it('should return correct type icon', () => {
    expect(component.getTypeIcon('BIOMETRIC')).toBe('fingerprint');
    expect(component.getTypeIcon('RFID')).toBe('contactless');
    expect(component.getTypeIcon('UNKNOWN')).toBe('devices');
  });

  it('should filter by status only', () => {
    deviceServiceSpy.getByStatus.and.returnValue(of(mockPage.content));
    component.selectedStatus = 'ACTIVE';
    component.selectedType = '';
    component.loadDevices();
    expect(deviceServiceSpy.getByStatus).toHaveBeenCalledWith('ACTIVE');
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should filter by type only', () => {
    deviceServiceSpy.getByType.and.returnValue(of(mockPage.content));
    component.selectedType = 'BIOMETRIC';
    component.selectedStatus = '';
    component.loadDevices();
    expect(deviceServiceSpy.getByType).toHaveBeenCalledWith('BIOMETRIC');
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should filter by both status and type', () => {
    deviceServiceSpy.getByStatus.and.returnValue(of(mockPage.content));
    component.selectedStatus = 'ACTIVE';
    component.selectedType = 'BIOMETRIC';
    component.loadDevices();
    expect(deviceServiceSpy.getByStatus).toHaveBeenCalledWith('ACTIVE');
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle status filter error', () => {
    deviceServiceSpy.getByStatus.and.returnValue(throwError(() => new Error('fail')));
    component.selectedStatus = 'ACTIVE';
    component.loadDevices();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load devices');
  });

  it('should handle type filter error', () => {
    deviceServiceSpy.getByType.and.returnValue(throwError(() => new Error('fail')));
    component.selectedType = 'RFID';
    component.loadDevices();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load devices');
  });

  it('should handle combined filter error', () => {
    deviceServiceSpy.getByStatus.and.returnValue(throwError(() => new Error('fail')));
    component.selectedStatus = 'ACTIVE';
    component.selectedType = 'BIOMETRIC';
    component.loadDevices();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load devices');
  });
});
