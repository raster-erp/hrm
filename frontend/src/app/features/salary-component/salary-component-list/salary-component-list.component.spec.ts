import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { SalaryComponentListComponent } from './salary-component-list.component';
import { SalaryComponentService } from '../../../services/salary-component.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { SalaryComponentResponse } from '../../../models/salary-component.model';

describe('SalaryComponentListComponent', () => {
  let component: SalaryComponentListComponent;
  let fixture: ComponentFixture<SalaryComponentListComponent>;
  let serviceSpy: jasmine.SpyObj<SalaryComponentService>;
  let notificationSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockPage: Page<SalaryComponentResponse> = {
    content: [
      {
        id: 1, code: 'BASIC', name: 'Basic Salary', type: 'EARNING',
        computationType: 'FIXED', percentageValue: null, taxable: true,
        mandatory: true, description: 'Basic salary', active: true,
        createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
      }
    ],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('SalaryComponentService', ['getAll', 'getByType', 'updateActive', 'delete']);
    notificationSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    serviceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        SalaryComponentListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: SalaryComponentService, useValue: serviceSpy },
        { provide: NotificationService, useValue: notificationSpy },
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

    fixture = TestBed.createComponent(SalaryComponentListComponent);
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

  it('should load components on init', () => {
    component.ngOnInit();
    expect(serviceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle load error', () => {
    serviceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadComponents();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to load salary components');
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
    component.selectedType = 'EARNING';
    component.clearFilters();
    expect(component.selectedType).toBe('');
  });

  it('should navigate to edit page', () => {
    component.editComponent(mockPage.content[0]);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/salary-components', 1, 'edit']);
  });

  it('should toggle active status', () => {
    serviceSpy.updateActive.and.returnValue(of(mockPage.content[0]));
    component.toggleActive(mockPage.content[0]);
    expect(serviceSpy.updateActive).toHaveBeenCalledWith(1, false);
    expect(notificationSpy.success).toHaveBeenCalled();
  });

  it('should handle toggle active error', () => {
    serviceSpy.updateActive.and.returnValue(throwError(() => new Error('fail')));
    component.toggleActive(mockPage.content[0]);
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to update component status');
  });

  it('should delete component after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    serviceSpy.delete.and.returnValue(of(void 0));
    component.deleteComponent(mockPage.content[0]);
    expect(serviceSpy.delete).toHaveBeenCalledWith(1);
    expect(notificationSpy.success).toHaveBeenCalled();
  });

  it('should not delete component if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteComponent(mockPage.content[0]);
    expect(serviceSpy.delete).not.toHaveBeenCalled();
  });

  it('should handle delete error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    serviceSpy.delete.and.returnValue(throwError(() => new Error('fail')));
    component.deleteComponent(mockPage.content[0]);
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to delete component');
  });

  it('should filter by type', () => {
    serviceSpy.getByType.and.returnValue(of(mockPage.content));
    component.selectedType = 'EARNING';
    component.loadComponents();
    expect(serviceSpy.getByType).toHaveBeenCalledWith('EARNING');
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle type filter error', () => {
    serviceSpy.getByType.and.returnValue(throwError(() => new Error('fail')));
    component.selectedType = 'EARNING';
    component.loadComponents();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to load salary components');
  });
});
