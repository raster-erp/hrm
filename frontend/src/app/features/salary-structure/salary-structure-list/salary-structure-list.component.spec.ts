import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { SalaryStructureListComponent } from './salary-structure-list.component';
import { SalaryStructureService } from '../../../services/salary-structure.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { SalaryStructureResponse } from '../../../models/salary-structure.model';

describe('SalaryStructureListComponent', () => {
  let component: SalaryStructureListComponent;
  let fixture: ComponentFixture<SalaryStructureListComponent>;
  let serviceSpy: jasmine.SpyObj<SalaryStructureService>;
  let notificationSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockPage: Page<SalaryStructureResponse> = {
    content: [
      {
        id: 1, code: 'STD', name: 'Standard', description: 'Standard structure',
        active: true, components: [],
        createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
      }
    ],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('SalaryStructureService', ['getAll', 'clone', 'updateActive', 'delete']);
    notificationSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    serviceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        SalaryStructureListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: SalaryStructureService, useValue: serviceSpy },
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

    fixture = TestBed.createComponent(SalaryStructureListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have displayed columns defined', () => {
    expect(component.displayedColumns.length).toBeGreaterThan(0);
  });

  it('should load structures on init', () => {
    component.ngOnInit();
    expect(serviceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle load error', () => {
    serviceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadStructures();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to load salary structures');
  });

  it('should update pagination on page change', () => {
    component.onPageChange({ pageIndex: 2, pageSize: 25, length: 100 });
    expect(component.pageIndex).toBe(2);
    expect(component.pageSize).toBe(25);
  });

  it('should navigate to edit page', () => {
    component.editStructure(mockPage.content[0]);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/salary-structures', 1, 'edit']);
  });

  it('should clone structure', () => {
    spyOn(window, 'prompt').and.returnValues('STD2', 'Standard Copy');
    serviceSpy.clone.and.returnValue(of(mockPage.content[0]));
    component.cloneStructure(mockPage.content[0]);
    expect(serviceSpy.clone).toHaveBeenCalledWith(1, { newCode: 'STD2', newName: 'Standard Copy' });
    expect(notificationSpy.success).toHaveBeenCalled();
  });

  it('should handle clone error', () => {
    spyOn(window, 'prompt').and.returnValues('STD2', 'Standard Copy');
    serviceSpy.clone.and.returnValue(throwError(() => new Error('fail')));
    component.cloneStructure(mockPage.content[0]);
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to clone structure');
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
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to update structure status');
  });

  it('should delete structure after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    serviceSpy.delete.and.returnValue(of(void 0));
    component.deleteStructure(mockPage.content[0]);
    expect(serviceSpy.delete).toHaveBeenCalledWith(1);
    expect(notificationSpy.success).toHaveBeenCalled();
  });

  it('should not delete structure if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteStructure(mockPage.content[0]);
    expect(serviceSpy.delete).not.toHaveBeenCalled();
  });

  it('should handle delete error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    serviceSpy.delete.and.returnValue(throwError(() => new Error('fail')));
    component.deleteStructure(mockPage.content[0]);
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to delete structure');
  });
});
