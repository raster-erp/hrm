import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LeaveTypeListComponent } from './leave-type-list.component';
import { LeaveTypeService } from '../../../services/leave-type.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { LeaveTypeResponse } from '../../../models/leave-type.model';

describe('LeaveTypeListComponent', () => {
  let component: LeaveTypeListComponent;
  let fixture: ComponentFixture<LeaveTypeListComponent>;
  let leaveTypeServiceSpy: jasmine.SpyObj<LeaveTypeService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockPage: Page<LeaveTypeResponse> = {
    content: [
      {
        id: 1, code: 'CL', name: 'Casual Leave', category: 'PAID',
        description: 'Casual leave', active: true,
        createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
      }
    ],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    leaveTypeServiceSpy = jasmine.createSpyObj('LeaveTypeService', ['getAll', 'getByCategory', 'updateActive', 'delete']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    leaveTypeServiceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        LeaveTypeListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: LeaveTypeService, useValue: leaveTypeServiceSpy },
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

    fixture = TestBed.createComponent(LeaveTypeListComponent);
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

  it('should load leave types on init', () => {
    component.ngOnInit();
    expect(leaveTypeServiceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle leave type load error', () => {
    leaveTypeServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadLeaveTypes();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load leave types');
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

  it('should clear category filter', () => {
    component.selectedCategory = 'PAID';
    component.clearFilters();
    expect(component.selectedCategory).toBe('');
  });

  it('should navigate to edit page', () => {
    component.editLeaveType(mockPage.content[0]);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/leave-types', 1, 'edit']);
  });

  it('should toggle active status', () => {
    leaveTypeServiceSpy.updateActive.and.returnValue(of(mockPage.content[0]));
    component.toggleActive(mockPage.content[0]);
    expect(leaveTypeServiceSpy.updateActive).toHaveBeenCalledWith(1, false);
    expect(notificationServiceSpy.success).toHaveBeenCalled();
  });

  it('should handle toggle active error', () => {
    leaveTypeServiceSpy.updateActive.and.returnValue(throwError(() => new Error('fail')));
    component.toggleActive(mockPage.content[0]);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to update leave type status');
  });

  it('should delete leave type after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    leaveTypeServiceSpy.delete.and.returnValue(of(void 0));
    component.deleteLeaveType(mockPage.content[0]);
    expect(leaveTypeServiceSpy.delete).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalled();
  });

  it('should not delete leave type if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteLeaveType(mockPage.content[0]);
    expect(leaveTypeServiceSpy.delete).not.toHaveBeenCalled();
  });

  it('should handle delete error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    leaveTypeServiceSpy.delete.and.returnValue(throwError(() => new Error('fail')));
    component.deleteLeaveType(mockPage.content[0]);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to delete leave type');
  });

  it('should filter by category', () => {
    leaveTypeServiceSpy.getByCategory.and.returnValue(of(mockPage.content));
    component.selectedCategory = 'PAID';
    component.loadLeaveTypes();
    expect(leaveTypeServiceSpy.getByCategory).toHaveBeenCalledWith('PAID');
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle category filter error', () => {
    leaveTypeServiceSpy.getByCategory.and.returnValue(throwError(() => new Error('fail')));
    component.selectedCategory = 'PAID';
    component.loadLeaveTypes();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load leave types');
  });
});
