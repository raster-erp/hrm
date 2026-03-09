import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ContractListComponent } from './contract-list.component';
import { ContractService } from '../../../services/contract.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { ContractResponse } from '../../../models/contract.model';

describe('ContractListComponent', () => {
  let component: ContractListComponent;
  let fixture: ComponentFixture<ContractListComponent>;
  let contractServiceSpy: jasmine.SpyObj<ContractService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockPage: Page<ContractResponse> = {
    content: [
      {
        id: 1, employeeId: 1, employeeCode: 'EMP001', employeeName: 'John Doe',
        contractType: 'PERMANENT', startDate: '2023-01-01', endDate: '2024-01-01',
        terms: 'Standard terms', status: 'ACTIVE', createdAt: '', updatedAt: ''
      }
    ],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    contractServiceSpy = jasmine.createSpyObj('ContractService', ['getAll', 'renew']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    contractServiceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        ContractListComponent,
        NoopAnimationsModule
      ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ContractService, useValue: contractServiceSpy },
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

    fixture = TestBed.createComponent(ContractListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have displayed columns defined', () => {
    expect(component.displayedColumns.length).toBe(6);
  });

  it('should have default page size of 10', () => {
    expect(component.pageSize).toBe(10);
  });

  it('should load contracts on init', () => {
    component.ngOnInit();
    expect(contractServiceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle contract load error', () => {
    contractServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadContracts();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load contracts');
  });

  it('should filter by status', () => {
    component.selectedStatus = 'ACTIVE';
    component.loadContracts();
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should filter out non-matching status', () => {
    component.selectedStatus = 'EXPIRED';
    component.loadContracts();
    expect(component.dataSource.data.length).toBe(0);
  });

  it('should reset page index on status filter', () => {
    component.pageIndex = 5;
    component.onStatusFilter();
    expect(component.pageIndex).toBe(0);
  });

  it('should update pagination on page change', () => {
    component.onPageChange({ pageIndex: 2, pageSize: 25, length: 100 });
    expect(component.pageIndex).toBe(2);
    expect(component.pageSize).toBe(25);
  });

  it('should clear all filters', () => {
    component.selectedStatus = 'ACTIVE';
    component.clearFilters();
    expect(component.selectedStatus).toBe('');
  });

  it('should navigate to contract detail on view', () => {
    component.viewContract(mockPage.content[0]);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/contracts', 1]);
  });

  it('should navigate to edit page', () => {
    component.editContract(mockPage.content[0]);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/contracts', 1, 'edit']);
  });

  it('should renew contract after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    contractServiceSpy.renew.and.returnValue(of(mockPage.content[0]));
    component.renewContract(mockPage.content[0]);
    expect(contractServiceSpy.renew).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalled();
  });

  it('should not renew contract if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.renewContract(mockPage.content[0]);
    expect(contractServiceSpy.renew).not.toHaveBeenCalled();
  });

  it('should handle renew error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    contractServiceSpy.renew.and.returnValue(throwError(() => new Error('fail')));
    component.renewContract(mockPage.content[0]);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to renew contract');
  });

  it('should return correct status color', () => {
    expect(component.getStatusColor('ACTIVE')).toBe('primary');
    expect(component.getStatusColor('EXPIRED')).toBe('warn');
    expect(component.getStatusColor('RENEWED')).toBe('accent');
    expect(component.getStatusColor('TERMINATED')).toBe('warn');
    expect(component.getStatusColor('UNKNOWN')).toBe('');
  });

  it('should format contract type', () => {
    expect(component.formatType('FIXED_TERM')).toBe('FIXED TERM');
    expect(component.formatType('PERMANENT')).toBe('PERMANENT');
  });
});
