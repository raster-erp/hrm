import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { DeviceService } from './device.service';
import { environment } from '../../environments/environment';
import { DeviceResponse } from '../models/device.model';
import { Page } from '../models/page.model';

describe('DeviceService', () => {
  let service: DeviceService;
  let httpMock: HttpTestingController;
  const baseUrl = environment.apiUrl;

  const mockDevice: DeviceResponse = {
    id: 1, serialNumber: 'BIO-001', name: 'Main Entrance',
    type: 'BIOMETRIC', location: 'Lobby', ipAddress: '192.168.1.100',
    status: 'ACTIVE', lastSyncAt: '2026-03-10T10:00:00',
    createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
  };

  const mockPage: Page<DeviceResponse> = {
    content: [mockDevice],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [DeviceService]
    });
    service = TestBed.inject(DeviceService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get all devices with pagination', () => {
    service.getAll(0, 10).subscribe(page => {
      expect(page.content.length).toBe(1);
      expect(page.totalElements).toBe(1);
    });

    const req = httpMock.expectOne(`${baseUrl}/devices?page=0&size=10`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);
  });

  it('should get device by id', () => {
    service.getById(1).subscribe(device => {
      expect(device.serialNumber).toBe('BIO-001');
    });

    const req = httpMock.expectOne(`${baseUrl}/devices/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockDevice);
  });

  it('should get device by serial number', () => {
    service.getBySerialNumber('BIO-001').subscribe(device => {
      expect(device.name).toBe('Main Entrance');
    });

    const req = httpMock.expectOne(`${baseUrl}/devices/serial/BIO-001`);
    expect(req.request.method).toBe('GET');
    req.flush(mockDevice);
  });

  it('should get devices by status', () => {
    service.getByStatus('ACTIVE').subscribe(devices => {
      expect(devices.length).toBe(1);
    });

    const req = httpMock.expectOne(`${baseUrl}/devices/status/ACTIVE`);
    expect(req.request.method).toBe('GET');
    req.flush([mockDevice]);
  });

  it('should get devices by type', () => {
    service.getByType('BIOMETRIC').subscribe(devices => {
      expect(devices.length).toBe(1);
    });

    const req = httpMock.expectOne(`${baseUrl}/devices/type/BIOMETRIC`);
    expect(req.request.method).toBe('GET');
    req.flush([mockDevice]);
  });

  it('should create device', () => {
    const request = {
      serialNumber: 'BIO-002', name: 'Back Door',
      type: 'BIOMETRIC', location: 'Rear', ipAddress: '192.168.1.101'
    };

    service.create(request).subscribe(device => {
      expect(device.serialNumber).toBe('BIO-001');
    });

    const req = httpMock.expectOne(`${baseUrl}/devices`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockDevice);
  });

  it('should update device', () => {
    const request = {
      serialNumber: 'BIO-001', name: 'Updated Name',
      type: 'BIOMETRIC', location: 'Lobby', ipAddress: '192.168.1.100'
    };

    service.update(1, request).subscribe(device => {
      expect(device.id).toBe(1);
    });

    const req = httpMock.expectOne(`${baseUrl}/devices/1`);
    expect(req.request.method).toBe('PUT');
    req.flush(mockDevice);
  });

  it('should update device status', () => {
    service.updateStatus(1, 'INACTIVE').subscribe(device => {
      expect(device.id).toBe(1);
    });

    const req = httpMock.expectOne(`${baseUrl}/devices/1/status`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ status: 'INACTIVE' });
    req.flush(mockDevice);
  });

  it('should record sync', () => {
    service.recordSync(1).subscribe(device => {
      expect(device.id).toBe(1);
    });

    const req = httpMock.expectOne(`${baseUrl}/devices/1/sync`);
    expect(req.request.method).toBe('POST');
    req.flush(mockDevice);
  });

  it('should delete device', () => {
    service.delete(1).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/devices/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
