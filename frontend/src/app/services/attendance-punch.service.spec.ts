import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AttendancePunchService } from './attendance-punch.service';
import { environment } from '../../environments/environment';
import { AttendancePunchResponse } from '../models/attendance-punch.model';
import { Page } from '../models/page.model';

describe('AttendancePunchService', () => {
  let service: AttendancePunchService;
  let httpMock: HttpTestingController;
  const baseUrl = environment.apiUrl;

  const mockPunch: AttendancePunchResponse = {
    id: 1, employeeId: 1, employeeCode: 'EMP001', employeeName: 'John Doe',
    deviceId: 1, deviceSerialNumber: 'BIO-001', deviceName: 'Main Entrance',
    punchTime: '2026-03-10T09:00:00', direction: 'IN', rawData: '',
    normalized: true, source: 'DEVICE',
    createdAt: '2026-03-10T09:00:00', updatedAt: '2026-03-10T09:00:00'
  };

  const mockPage: Page<AttendancePunchResponse> = {
    content: [mockPunch],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AttendancePunchService]
    });
    service = TestBed.inject(AttendancePunchService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get all punches with pagination', () => {
    service.getAll(0, 10).subscribe(page => {
      expect(page.content.length).toBe(1);
      expect(page.totalElements).toBe(1);
    });

    const req = httpMock.expectOne(`${baseUrl}/attendance-punches?page=0&size=10`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);
  });

  it('should get punch by id', () => {
    service.getById(1).subscribe(punch => {
      expect(punch.employeeCode).toBe('EMP001');
    });

    const req = httpMock.expectOne(`${baseUrl}/attendance-punches/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPunch);
  });

  it('should get punches by employee', () => {
    service.getByEmployee(1, 0, 10).subscribe(page => {
      expect(page.content.length).toBe(1);
    });

    const req = httpMock.expectOne(`${baseUrl}/attendance-punches/employee/1?page=0&size=10`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);
  });

  it('should get punches by date range', () => {
    service.getByDateRange('2026-03-01', '2026-03-10', 0, 10).subscribe(page => {
      expect(page.content.length).toBe(1);
    });

    const req = httpMock.expectOne(`${baseUrl}/attendance-punches/date-range?from=2026-03-01&to=2026-03-10&page=0&size=10`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);
  });

  it('should get punches by employee and date range', () => {
    service.getByEmployeeAndDateRange(1, '2026-03-01', '2026-03-10', 0, 10).subscribe(page => {
      expect(page.content.length).toBe(1);
    });

    const req = httpMock.expectOne(`${baseUrl}/attendance-punches/employee/1/date-range?from=2026-03-01&to=2026-03-10&page=0&size=10`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);
  });

  it('should create punch', () => {
    const request = {
      employeeId: 1, deviceId: 1,
      punchTime: '2026-03-10T09:00:00', direction: 'IN', rawData: ''
    };

    service.create(request).subscribe(punch => {
      expect(punch.id).toBe(1);
    });

    const req = httpMock.expectOne(`${baseUrl}/attendance-punches`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockPunch);
  });

  it('should delete punch', () => {
    service.delete(1).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/attendance-punches/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
