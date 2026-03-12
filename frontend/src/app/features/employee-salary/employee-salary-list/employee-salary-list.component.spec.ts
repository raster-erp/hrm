import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { EmployeeSalaryListComponent } from './employee-salary-list.component';
import { EmployeeSalaryService } from '../../../services/employee-salary.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { EmployeeSalaryDetailResponse } from '../../../models/employee-salary.model';

describe('EmployeeSalaryListComponent', () => {
  let component: EmployeeSalaryListComponent;
  let fixture: ComponentFixture<EmployeeSalaryListComponent>;
  let serviceSpy: jasmine.SpyObj<EmployeeSalaryService>;
  let notificationSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockPage: Page<EmployeeSalaryDetailResponse> = {
    content: [
      {
        id: 1, employeeId: 1, employeeName: 'John Doe', employeeCode: 'EMP001',
        salaryStructureId: 1, salaryStructureName: 'Standard',
        ctc: 1200000, basicSalary: 50000,
        effectiveDate: '2024-04-01', notes: 'Initial',
        active: true,
        createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
      }
    ],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('EmployeeSalaryService', ['getAll', 'delete']);
    notificationSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    serviceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        EmployeeSalaryListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: EmployeeSalaryService, useValue: serviceSpy },
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

    fixture = TestBed.createComponent(EmployeeSalaryListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have displayed columns defined', () => {
    expect(component.displayedColumns.length).toBeGreaterThan(0);
  });

  it('should load details on init', () => {
    component.ngOnInit();
    expect(serviceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle load error', () => {
    serviceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadDetails();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to load salary assignments');
  });

  it('should update pagination on page change', () => {
    component.onPageChange({ pageIndex: 2, pageSize: 25, length: 100 });
    expect(component.pageIndex).toBe(2);
    expect(component.pageSize).toBe(25);
  });

  it('should delete detail after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    serviceSpy.delete.and.returnValue(of(void 0));
    component.deleteDetail(mockPage.content[0]);
    expect(serviceSpy.delete).toHaveBeenCalledWith(1);
    expect(notificationSpy.success).toHaveBeenCalled();
  });

  it('should not delete detail if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteDetail(mockPage.content[0]);
    expect(serviceSpy.delete).not.toHaveBeenCalled();
  });

  it('should handle delete error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    serviceSpy.delete.and.returnValue(throwError(() => new Error('fail')));
    component.deleteDetail(mockPage.content[0]);
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to delete salary assignment');
  });
});
