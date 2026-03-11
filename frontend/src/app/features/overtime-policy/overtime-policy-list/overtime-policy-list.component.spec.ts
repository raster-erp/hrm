import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { OvertimePolicyListComponent } from './overtime-policy-list.component';
import { OvertimePolicyService } from '../../../services/overtime-policy.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { OvertimePolicyResponse } from '../../../models/overtime-policy.model';

describe('OvertimePolicyListComponent', () => {
  let component: OvertimePolicyListComponent;
  let fixture: ComponentFixture<OvertimePolicyListComponent>;
  let overtimePolicyServiceSpy: jasmine.SpyObj<OvertimePolicyService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockPage: Page<OvertimePolicyResponse> = {
    content: [
      {
        id: 1, name: 'Weekday OT', type: 'WEEKDAY',
        rateMultiplier: 1.5, minOvertimeMinutes: 30,
        maxOvertimeMinutesPerDay: 120, maxOvertimeMinutesPerMonth: 2400,
        requiresApproval: true, active: true,
        description: 'Weekday overtime policy',
        createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
      }
    ],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    overtimePolicyServiceSpy = jasmine.createSpyObj('OvertimePolicyService', ['getAll', 'getByType', 'updateActive', 'delete']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    overtimePolicyServiceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        OvertimePolicyListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: OvertimePolicyService, useValue: overtimePolicyServiceSpy },
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

    fixture = TestBed.createComponent(OvertimePolicyListComponent);
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
    expect(overtimePolicyServiceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle policy load error', () => {
    overtimePolicyServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadPolicies();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load overtime policies');
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

  it('should clear type filter', () => {
    component.selectedType = 'WEEKDAY';
    component.clearFilters();
    expect(component.selectedType).toBe('');
  });

  it('should navigate to edit page', () => {
    component.editPolicy(mockPage.content[0]);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/overtime-policies', 1, 'edit']);
  });

  it('should toggle active status', () => {
    overtimePolicyServiceSpy.updateActive.and.returnValue(of(mockPage.content[0]));
    component.toggleActive(mockPage.content[0]);
    expect(overtimePolicyServiceSpy.updateActive).toHaveBeenCalledWith(1, false);
    expect(notificationServiceSpy.success).toHaveBeenCalled();
  });

  it('should handle toggle active error', () => {
    overtimePolicyServiceSpy.updateActive.and.returnValue(throwError(() => new Error('fail')));
    component.toggleActive(mockPage.content[0]);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to update policy status');
  });

  it('should delete policy after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    overtimePolicyServiceSpy.delete.and.returnValue(of(void 0));
    component.deletePolicy(mockPage.content[0]);
    expect(overtimePolicyServiceSpy.delete).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalled();
  });

  it('should not delete policy if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deletePolicy(mockPage.content[0]);
    expect(overtimePolicyServiceSpy.delete).not.toHaveBeenCalled();
  });

  it('should handle delete error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    overtimePolicyServiceSpy.delete.and.returnValue(throwError(() => new Error('fail')));
    component.deletePolicy(mockPage.content[0]);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to delete policy');
  });

  it('should filter by type', () => {
    overtimePolicyServiceSpy.getByType.and.returnValue(of(mockPage.content));
    component.selectedType = 'WEEKDAY';
    component.loadPolicies();
    expect(overtimePolicyServiceSpy.getByType).toHaveBeenCalledWith('WEEKDAY');
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle type filter error', () => {
    overtimePolicyServiceSpy.getByType.and.returnValue(throwError(() => new Error('fail')));
    component.selectedType = 'WEEKDAY';
    component.loadPolicies();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load overtime policies');
  });
});
