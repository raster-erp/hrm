import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { DeviceFormComponent } from './device-form.component';
import { DeviceService } from '../../../services/device.service';
import { NotificationService } from '../../../services/notification.service';
import { DeviceResponse } from '../../../models/device.model';

describe('DeviceFormComponent', () => {
  let component: DeviceFormComponent;
  let fixture: ComponentFixture<DeviceFormComponent>;
  let deviceServiceSpy: jasmine.SpyObj<DeviceService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockDevice: DeviceResponse = {
    id: 1, serialNumber: 'BIO-001', name: 'Main Entrance',
    type: 'BIOMETRIC', location: 'Lobby', ipAddress: '192.168.1.100',
    status: 'ACTIVE', lastSyncAt: '2026-03-10T10:00:00',
    createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
  };

  function setup(routeId: string | null = null) {
    deviceServiceSpy = jasmine.createSpyObj('DeviceService', ['getById', 'create', 'update']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [
        DeviceFormComponent,
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
            snapshot: { paramMap: { get: () => routeId } }
          }
        }
      ]
    });

    fixture = TestBed.createComponent(DeviceFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  describe('create mode', () => {
    beforeEach(() => setup(null));

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should be in create mode by default', () => {
      expect(component.isEditMode).toBeFalse();
    });

    it('should initialize form group', () => {
      expect(component.deviceForm).toBeDefined();
    });

    it('should require serial number', () => {
      expect(component.deviceForm.get('serialNumber')?.hasError('required')).toBeTrue();
    });

    it('should require name', () => {
      expect(component.deviceForm.get('name')?.hasError('required')).toBeTrue();
    });

    it('should require type', () => {
      expect(component.deviceForm.get('type')?.hasError('required')).toBeTrue();
    });

    it('should show error if submitting invalid form', () => {
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Please fill in all required fields correctly');
    });

    it('should create device on valid submit', () => {
      fillValidForm();
      deviceServiceSpy.create.and.returnValue(of(mockDevice));
      component.onSubmit();
      expect(deviceServiceSpy.create).toHaveBeenCalled();
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Device created successfully');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/devices']);
    });

    it('should handle create error', () => {
      fillValidForm();
      deviceServiceSpy.create.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to create device');
    });

    it('should navigate back on cancel', () => {
      component.onCancel();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/devices']);
    });
  });

  describe('edit mode', () => {
    beforeEach(() => {
      deviceServiceSpy = jasmine.createSpyObj('DeviceService', ['getById', 'create', 'update']);
      notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
      routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      deviceServiceSpy.getById.and.returnValue(of(mockDevice));

      TestBed.configureTestingModule({
        imports: [DeviceFormComponent, HttpClientTestingModule, NoopAnimationsModule],
        providers: [
          { provide: DeviceService, useValue: deviceServiceSpy },
          { provide: NotificationService, useValue: notificationServiceSpy },
          { provide: Router, useValue: routerSpy },
          { provide: ActivatedRoute, useValue: { params: of({}), snapshot: { paramMap: { get: () => '1' } } } }
        ]
      });

      fixture = TestBed.createComponent(DeviceFormComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should be in edit mode', () => {
      expect(component.isEditMode).toBeTrue();
      expect(component.deviceId).toBe(1);
    });

    it('should load device data', () => {
      expect(deviceServiceSpy.getById).toHaveBeenCalledWith(1);
      expect(component.deviceForm.get('serialNumber')?.value).toBe('BIO-001');
    });

    it('should update device on submit', () => {
      deviceServiceSpy.update.and.returnValue(of(mockDevice));
      component.onSubmit();
      expect(deviceServiceSpy.update).toHaveBeenCalledWith(1, jasmine.any(Object));
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Device updated successfully');
    });

    it('should handle update error', () => {
      deviceServiceSpy.update.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to update device');
    });

    it('should handle load device error', () => {
      deviceServiceSpy.getById.and.returnValue(throwError(() => new Error('fail')));
      component.loadDevice(999);
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load device');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/devices']);
    });
  });

  function fillValidForm() {
    component.deviceForm.patchValue({
      serialNumber: 'BIO-001',
      name: 'Main Entrance',
      type: 'BIOMETRIC',
      location: 'Lobby',
      ipAddress: '192.168.1.100'
    });
  }
});
