import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { WfhRequestListComponent } from './wfh-request-list.component';
import { WfhRequestService } from '../../../services/wfh-request.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { WfhRequestResponse } from '../../../models/wfh-request.model';

describe('WfhRequestListComponent', () => {
  let component: WfhRequestListComponent;
  let fixture: ComponentFixture<WfhRequestListComponent>;
  let wfhRequestServiceSpy: jasmine.SpyObj<WfhRequestService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockRecord: WfhRequestResponse = {
    id: 1, employeeId: 100, employeeCode: 'EMP001', employeeName: 'John Doe',
    requestDate: '2026-01-15', reason: 'Personal work', status: 'PENDING',
    approvedBy: null, approvedAt: null, remarks: 'Test',
    createdAt: '2026-01-15T00:00:00', updatedAt: '2026-01-15T00:00:00'
  };

  const mockPage: Page<WfhRequestResponse> = {
    content: [mockRecord],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    wfhRequestServiceSpy = jasmine.createSpyObj('WfhRequestService',
      ['getAll', 'getByStatus', 'approve', 'delete']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    wfhRequestServiceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        WfhRequestListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: WfhRequestService, useValue: wfhRequestServiceSpy },
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

    fixture = TestBed.createComponent(WfhRequestListComponent);
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

  it('should load records on init', () => {
    component.ngOnInit();
    expect(wfhRequestServiceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle record load error', () => {
    wfhRequestServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadRecords();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load WFH requests');
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

  it('should clear status filter', () => {
    component.selectedStatus = 'PENDING';
    component.clearFilters();
    expect(component.selectedStatus).toBe('');
  });

  it('should navigate to edit page', () => {
    component.editRecord(mockRecord);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/wfh-requests', 1, 'edit']);
  });

  it('should filter by status', () => {
    wfhRequestServiceSpy.getByStatus.and.returnValue(of(mockPage));
    component.selectedStatus = 'PENDING';
    component.loadRecords();
    expect(wfhRequestServiceSpy.getByStatus).toHaveBeenCalledWith('PENDING', 0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle status filter error', () => {
    wfhRequestServiceSpy.getByStatus.and.returnValue(throwError(() => new Error('fail')));
    component.selectedStatus = 'PENDING';
    component.loadRecords();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load WFH requests');
  });

  it('should approve record', () => {
    wfhRequestServiceSpy.approve.and.returnValue(of(mockRecord));
    component.approveRecord(mockRecord);
    expect(wfhRequestServiceSpy.approve).toHaveBeenCalledWith(1, {
      status: 'APPROVED', approvedBy: 'admin', remarks: ''
    });
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('WFH request approved successfully');
  });

  it('should handle approve error', () => {
    wfhRequestServiceSpy.approve.and.returnValue(throwError(() => new Error('fail')));
    component.approveRecord(mockRecord);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to approve WFH request');
  });

  it('should reject record', () => {
    wfhRequestServiceSpy.approve.and.returnValue(of(mockRecord));
    component.rejectRecord(mockRecord);
    expect(wfhRequestServiceSpy.approve).toHaveBeenCalledWith(1, {
      status: 'REJECTED', approvedBy: 'admin', remarks: ''
    });
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('WFH request rejected successfully');
  });

  it('should handle reject error', () => {
    wfhRequestServiceSpy.approve.and.returnValue(throwError(() => new Error('fail')));
    component.rejectRecord(mockRecord);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to reject WFH request');
  });

  it('should delete record after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    wfhRequestServiceSpy.delete.and.returnValue(of(void 0));
    component.deleteRecord(mockRecord);
    expect(wfhRequestServiceSpy.delete).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalled();
  });

  it('should not delete record if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteRecord(mockRecord);
    expect(wfhRequestServiceSpy.delete).not.toHaveBeenCalled();
  });

  it('should handle delete error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    wfhRequestServiceSpy.delete.and.returnValue(throwError(() => new Error('fail')));
    component.deleteRecord(mockRecord);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to delete WFH request');
  });
});
