import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LeavePolicyAssignmentListComponent } from './leave-policy-assignment-list.component';
import { LeavePolicyAssignmentService } from '../../../services/leave-policy-assignment.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { LeavePolicyAssignmentResponse } from '../../../models/leave-policy-assignment.model';

describe('LeavePolicyAssignmentListComponent', () => {
  let component: LeavePolicyAssignmentListComponent;
  let fixture: ComponentFixture<LeavePolicyAssignmentListComponent>;
  let assignmentServiceSpy: jasmine.SpyObj<LeavePolicyAssignmentService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockPage: Page<LeavePolicyAssignmentResponse> = {
    content: [
      {
        id: 1, leavePolicyId: 1, leavePolicyName: 'Standard CL Policy',
        assignmentType: 'DEPARTMENT', departmentId: 1, departmentName: 'Engineering',
        designationId: null, designationTitle: null, employeeId: null, employeeName: null,
        effectiveFrom: '2026-01-01', effectiveTo: null,
        active: true,
        createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
      }
    ],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    assignmentServiceSpy = jasmine.createSpyObj('LeavePolicyAssignmentService', ['getAll', 'getByType', 'updateActive', 'delete']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    assignmentServiceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        LeavePolicyAssignmentListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: LeavePolicyAssignmentService, useValue: assignmentServiceSpy },
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

    fixture = TestBed.createComponent(LeavePolicyAssignmentListComponent);
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

  it('should load assignments on init', () => {
    component.ngOnInit();
    expect(assignmentServiceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle assignment load error', () => {
    assignmentServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadAssignments();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load policy assignments');
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
    component.selectedType = 'DEPARTMENT';
    component.clearFilters();
    expect(component.selectedType).toBe('');
  });

  it('should navigate to edit page', () => {
    component.editAssignment(mockPage.content[0]);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/leave-policy-assignments', 1, 'edit']);
  });

  it('should toggle active status', () => {
    assignmentServiceSpy.updateActive.and.returnValue(of(mockPage.content[0]));
    component.toggleActive(mockPage.content[0]);
    expect(assignmentServiceSpy.updateActive).toHaveBeenCalledWith(1, false);
    expect(notificationServiceSpy.success).toHaveBeenCalled();
  });

  it('should handle toggle active error', () => {
    assignmentServiceSpy.updateActive.and.returnValue(throwError(() => new Error('fail')));
    component.toggleActive(mockPage.content[0]);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to update assignment status');
  });

  it('should delete assignment after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    assignmentServiceSpy.delete.and.returnValue(of(void 0));
    component.deleteAssignment(mockPage.content[0]);
    expect(assignmentServiceSpy.delete).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalled();
  });

  it('should not delete assignment if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteAssignment(mockPage.content[0]);
    expect(assignmentServiceSpy.delete).not.toHaveBeenCalled();
  });

  it('should handle delete error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    assignmentServiceSpy.delete.and.returnValue(throwError(() => new Error('fail')));
    component.deleteAssignment(mockPage.content[0]);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to delete assignment');
  });

  it('should filter by type', () => {
    assignmentServiceSpy.getByType.and.returnValue(of(mockPage.content));
    component.selectedType = 'DEPARTMENT';
    component.loadAssignments();
    expect(assignmentServiceSpy.getByType).toHaveBeenCalledWith('DEPARTMENT');
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle type filter error', () => {
    assignmentServiceSpy.getByType.and.returnValue(throwError(() => new Error('fail')));
    component.selectedType = 'DEPARTMENT';
    component.loadAssignments();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load policy assignments');
  });
});
