import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { TaxSlabListComponent } from './tax-slab-list.component';
import { TaxSlabService } from '../../../services/tax-slab.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { TaxSlabResponse } from '../../../models/tax-slab.model';

describe('TaxSlabListComponent', () => {
  let component: TaxSlabListComponent;
  let fixture: ComponentFixture<TaxSlabListComponent>;
  let serviceSpy: jasmine.SpyObj<TaxSlabService>;
  let notificationSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockPage: Page<TaxSlabResponse> = {
    content: [
      {
        id: 1, regime: 'NEW', financialYear: '2025-26',
        slabFrom: 0, slabTo: 300000, rate: 0,
        description: 'No tax', active: true,
        createdAt: '2026-01-01T00:00:00', updatedAt: '2026-01-01T00:00:00'
      }
    ],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('TaxSlabService', ['getAll', 'getByRegimeAndYear', 'delete']);
    notificationSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    serviceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        TaxSlabListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: TaxSlabService, useValue: serviceSpy },
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

    fixture = TestBed.createComponent(TaxSlabListComponent);
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

  it('should load slabs on init', () => {
    component.ngOnInit();
    expect(serviceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle load error', () => {
    serviceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadSlabs();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to load tax slabs');
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

  it('should clear filters', () => {
    component.selectedRegime = 'NEW';
    component.financialYear = '2025-26';
    component.clearFilters();
    expect(component.selectedRegime).toBe('');
    expect(component.financialYear).toBe('');
  });

  it('should navigate to edit page', () => {
    component.editSlab(mockPage.content[0]);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/tax-slabs', 1, 'edit']);
  });

  it('should delete slab after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    serviceSpy.delete.and.returnValue(of(void 0));
    component.deleteSlab(mockPage.content[0]);
    expect(serviceSpy.delete).toHaveBeenCalledWith(1);
    expect(notificationSpy.success).toHaveBeenCalled();
  });

  it('should not delete slab if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteSlab(mockPage.content[0]);
    expect(serviceSpy.delete).not.toHaveBeenCalled();
  });

  it('should handle delete error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    serviceSpy.delete.and.returnValue(throwError(() => new Error('fail')));
    component.deleteSlab(mockPage.content[0]);
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to delete tax slab');
  });

  it('should filter by regime and year', () => {
    serviceSpy.getByRegimeAndYear.and.returnValue(of(mockPage.content));
    component.selectedRegime = 'NEW';
    component.financialYear = '2025-26';
    component.loadSlabs();
    expect(serviceSpy.getByRegimeAndYear).toHaveBeenCalledWith('NEW', '2025-26');
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle regime and year filter error', () => {
    serviceSpy.getByRegimeAndYear.and.returnValue(throwError(() => new Error('fail')));
    component.selectedRegime = 'NEW';
    component.financialYear = '2025-26';
    component.loadSlabs();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to load tax slabs');
  });
});
