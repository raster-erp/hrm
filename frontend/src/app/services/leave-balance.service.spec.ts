import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { LeaveBalanceService } from './leave-balance.service';
import { environment } from '../../environments/environment';

describe('LeaveBalanceService', () => {
  let service: LeaveBalanceService;
  let httpMock: HttpTestingController;
  const baseUrl = environment.apiUrl;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [LeaveBalanceService]
    });
    service = TestBed.inject(LeaveBalanceService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get balances by employee', () => {
    const mockBalances = [{ id: 1, employeeId: 1, leaveTypeName: 'CL', credited: 12 }];
    service.getBalancesByEmployee(1, 2025).subscribe(data => {
      expect(data).toEqual(mockBalances as any);
    });
    const req = httpMock.expectOne(`${baseUrl}/leave-balances/employee/1?year=2025`);
    expect(req.request.method).toBe('GET');
    req.flush(mockBalances);
  });

  it('should get single balance', () => {
    const mockBalance = { id: 1, employeeId: 1, leaveTypeId: 1, credited: 12 };
    service.getBalance(1, 1, 2025).subscribe(data => {
      expect(data).toEqual(mockBalance as any);
    });
    const req = httpMock.expectOne(`${baseUrl}/leave-balances/employee/1/leave-type/1?year=2025`);
    expect(req.request.method).toBe('GET');
    req.flush(mockBalance);
  });

  it('should get transactions', () => {
    const mockPage = { content: [{ id: 1, transactionType: 'CREDIT' }], totalElements: 1 };
    service.getTransactions(1, 0, 10).subscribe(data => {
      expect(data).toEqual(mockPage as any);
    });
    const req = httpMock.expectOne(`${baseUrl}/leave-balances/employee/1/transactions?page=0&size=10`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);
  });

  it('should get transactions with filters', () => {
    const mockPage = { content: [], totalElements: 0 };
    service.getTransactions(1, 0, 10, 1, 'CREDIT').subscribe(data => {
      expect(data).toEqual(mockPage as any);
    });
    const req = httpMock.expectOne(
      `${baseUrl}/leave-balances/employee/1/transactions?page=0&size=10&leaveTypeId=1&transactionType=CREDIT`
    );
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);
  });

  it('should adjust balance', () => {
    const request = { employeeId: 1, leaveTypeId: 1, year: 2025, amount: 5, description: null, adjustedBy: null };
    const mockResponse = { id: 1, credited: 17 };
    service.adjustBalance(request).subscribe(data => {
      expect(data).toEqual(mockResponse as any);
    });
    const req = httpMock.expectOne(`${baseUrl}/leave-balances/adjust`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('should process year end', () => {
    const request = { year: 2025, processedBy: 'Admin' };
    const mockResponse = { processedYear: 2025, nextYear: 2026, employeesProcessed: 10 };
    service.processYearEnd(request).subscribe(data => {
      expect(data).toEqual(mockResponse as any);
    });
    const req = httpMock.expectOne(`${baseUrl}/leave-balances/year-end`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });
});
