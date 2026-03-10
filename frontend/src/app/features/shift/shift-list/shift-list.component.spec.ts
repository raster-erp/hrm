import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ShiftListComponent } from './shift-list.component';
import { ShiftService } from '../../../services/shift.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { ShiftResponse } from '../../../models/shift.model';

describe('ShiftListComponent', () => {
  let component: ShiftListComponent;
  let fixture: ComponentFixture<ShiftListComponent>;
  let shiftServiceSpy: jasmine.SpyObj<ShiftService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockPage: Page<ShiftResponse> = {
    content: [
      {
        id: 1, name: 'Morning Shift', type: 'MORNING',
        startTime: '06:00', endTime: '14:00',
        breakDurationMinutes: 30, gracePeriodMinutes: 10,
        description: 'Morning shift', active: true,
        createdAt: '2026-01-01T00:00:00', updatedAt: '2026-03-10T10:00:00'
      }
    ],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    shiftServiceSpy = jasmine.createSpyObj('ShiftService', ['getAll', 'getByType', 'updateActive', 'delete']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    shiftServiceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        ShiftListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: ShiftService, useValue: shiftServiceSpy },
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

    fixture = TestBed.createComponent(ShiftListComponent);
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

  it('should load shifts on init', () => {
    component.ngOnInit();
    expect(shiftServiceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle shift load error', () => {
    shiftServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadShifts();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load shifts');
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
    component.selectedType = 'MORNING';
    component.clearFilters();
    expect(component.selectedType).toBe('');
  });

  it('should navigate to edit page', () => {
    component.editShift(mockPage.content[0]);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/shifts', 1, 'edit']);
  });

  it('should toggle active status', () => {
    shiftServiceSpy.updateActive.and.returnValue(of(mockPage.content[0]));
    component.toggleActive(mockPage.content[0]);
    expect(shiftServiceSpy.updateActive).toHaveBeenCalledWith(1, false);
    expect(notificationServiceSpy.success).toHaveBeenCalled();
  });

  it('should handle toggle active error', () => {
    shiftServiceSpy.updateActive.and.returnValue(throwError(() => new Error('fail')));
    component.toggleActive(mockPage.content[0]);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to update shift status');
  });

  it('should delete shift after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    shiftServiceSpy.delete.and.returnValue(of(void 0));
    component.deleteShift(mockPage.content[0]);
    expect(shiftServiceSpy.delete).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalled();
  });

  it('should not delete shift if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteShift(mockPage.content[0]);
    expect(shiftServiceSpy.delete).not.toHaveBeenCalled();
  });

  it('should handle delete error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    shiftServiceSpy.delete.and.returnValue(throwError(() => new Error('fail')));
    component.deleteShift(mockPage.content[0]);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to delete shift');
  });

  it('should filter by type', () => {
    shiftServiceSpy.getByType.and.returnValue(of(mockPage.content));
    component.selectedType = 'MORNING';
    component.loadShifts();
    expect(shiftServiceSpy.getByType).toHaveBeenCalledWith('MORNING');
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle type filter error', () => {
    shiftServiceSpy.getByType.and.returnValue(throwError(() => new Error('fail')));
    component.selectedType = 'MORNING';
    component.loadShifts();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load shifts');
  });
});
