import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LeavePolicyListComponent } from './leave-policy-list.component';
import { LeavePolicyService } from '../../../services/leave-policy.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { LeavePolicyResponse } from '../../../models/leave-policy.model';

describe('LeavePolicyListComponent', () => {
  let component: LeavePolicyListComponent;
  let fixture: ComponentFixture<LeavePolicyListComponent>;
  let leavePolicyServiceSpy: jasmine.SpyObj<LeavePolicyService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockPage: Page<LeavePolicyResponse> = {
    content: [
      {
        id: 1, name: 'Standard CL Policy', leaveTypeId: 1,
        leaveTypeName: 'Casual Leave', leaveTypeCode: 'CL',
        accrualFrequency: 'MONTHLY', accrualDays: 1.5,
        maxAccumulation: 12, carryForwardLimit: 5,
        proRataForNewJoiners: true, minServiceDaysRequired: 0,
        active: true, description: 'Standard casual leave policy',
        createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
      }
    ],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    leavePolicyServiceSpy = jasmine.createSpyObj('LeavePolicyService', ['getAll', 'updateActive', 'delete']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    leavePolicyServiceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        LeavePolicyListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: LeavePolicyService, useValue: leavePolicyServiceSpy },
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

    fixture = TestBed.createComponent(LeavePolicyListComponent);
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

  it('should load policies on init', () => {
    component.ngOnInit();
    expect(leavePolicyServiceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle policy load error', () => {
    leavePolicyServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadPolicies();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load leave policies');
  });

  it('should update pagination on page change', () => {
    component.onPageChange({ pageIndex: 2, pageSize: 25, length: 100 });
    expect(component.pageIndex).toBe(2);
    expect(component.pageSize).toBe(25);
  });

  it('should navigate to edit page', () => {
    component.editPolicy(mockPage.content[0]);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/leave-policies', 1, 'edit']);
  });

  it('should toggle active status', () => {
    leavePolicyServiceSpy.updateActive.and.returnValue(of(mockPage.content[0]));
    component.toggleActive(mockPage.content[0]);
    expect(leavePolicyServiceSpy.updateActive).toHaveBeenCalledWith(1, false);
    expect(notificationServiceSpy.success).toHaveBeenCalled();
  });

  it('should handle toggle active error', () => {
    leavePolicyServiceSpy.updateActive.and.returnValue(throwError(() => new Error('fail')));
    component.toggleActive(mockPage.content[0]);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to update policy status');
  });

  it('should delete policy after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    leavePolicyServiceSpy.delete.and.returnValue(of(void 0));
    component.deletePolicy(mockPage.content[0]);
    expect(leavePolicyServiceSpy.delete).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalled();
  });

  it('should not delete policy if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deletePolicy(mockPage.content[0]);
    expect(leavePolicyServiceSpy.delete).not.toHaveBeenCalled();
  });

  it('should handle delete error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    leavePolicyServiceSpy.delete.and.returnValue(throwError(() => new Error('fail')));
    component.deletePolicy(mockPage.content[0]);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to delete policy');
  });
});
