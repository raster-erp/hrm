import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { HolidaySetupComponent } from './holiday-setup.component';
import { HolidayService } from '../../../services/holiday.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { HolidayResponse } from '../../../models/holiday.model';

describe('HolidaySetupComponent', () => {
  let component: HolidaySetupComponent;
  let fixture: ComponentFixture<HolidaySetupComponent>;
  let holidayServiceSpy: jasmine.SpyObj<HolidayService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  const mockHoliday: HolidayResponse = {
    id: 1, name: 'Republic Day', date: '2026-01-26', type: 'PUBLIC',
    region: null, description: 'National holiday', active: true,
    createdAt: '2026-01-01T00:00:00', updatedAt: '2026-01-01T00:00:00'
  };

  const mockPage: Page<HolidayResponse> = {
    content: [mockHoliday],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    holidayServiceSpy = jasmine.createSpyObj('HolidayService',
      ['getAll', 'getByType', 'create', 'update', 'deactivate']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    holidayServiceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        HolidaySetupComponent,
        HttpClientTestingModule,
        NoopAnimationsModule,
        RouterTestingModule
      ],
      providers: [
        { provide: HolidayService, useValue: holidayServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HolidaySetupComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load records on init', () => {
    component.ngOnInit();
    expect(holidayServiceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle load error', () => {
    holidayServiceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadRecords();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load holidays');
  });

  it('should filter by type', () => {
    holidayServiceSpy.getByType.and.returnValue(of(mockPage));
    component.selectedType = 'PUBLIC';
    component.loadRecords();
    expect(holidayServiceSpy.getByType).toHaveBeenCalledWith('PUBLIC', 0, 10);
  });

  it('should clear filters', () => {
    component.selectedType = 'PUBLIC';
    component.clearFilters();
    expect(component.selectedType).toBe('');
  });

  it('should open new form', () => {
    component.ngOnInit();
    component.openNewForm();
    expect(component.showForm).toBeTrue();
    expect(component.editingId).toBeNull();
  });

  it('should open edit form', () => {
    component.ngOnInit();
    component.openEditForm(mockHoliday);
    expect(component.showForm).toBeTrue();
    expect(component.editingId).toBe(1);
  });

  it('should cancel form', () => {
    component.ngOnInit();
    component.showForm = true;
    component.editingId = 1;
    component.cancelForm();
    expect(component.showForm).toBeFalse();
    expect(component.editingId).toBeNull();
  });

  it('should not submit invalid form', () => {
    component.ngOnInit();
    component.onSubmit();
    expect(holidayServiceSpy.create).not.toHaveBeenCalled();
  });

  it('should submit new holiday', () => {
    holidayServiceSpy.create.and.returnValue(of(mockHoliday));
    component.ngOnInit();
    component.form.patchValue({
      name: 'Republic Day',
      date: '2026-01-26',
      type: 'PUBLIC'
    });
    component.onSubmit();
    expect(holidayServiceSpy.create).toHaveBeenCalled();
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Holiday created successfully');
  });

  it('should update existing holiday', () => {
    holidayServiceSpy.update.and.returnValue(of(mockHoliday));
    component.ngOnInit();
    component.editingId = 1;
    component.form.patchValue({
      name: 'Republic Day',
      date: '2026-01-26',
      type: 'PUBLIC'
    });
    component.onSubmit();
    expect(holidayServiceSpy.update).toHaveBeenCalledWith(1, jasmine.any(Object));
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Holiday updated successfully');
  });

  it('should handle create error', () => {
    holidayServiceSpy.create.and.returnValue(throwError(() => new Error('fail')));
    component.ngOnInit();
    component.form.patchValue({
      name: 'Republic Day',
      date: '2026-01-26',
      type: 'PUBLIC'
    });
    component.onSubmit();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to create holiday');
  });

  it('should deactivate holiday', () => {
    holidayServiceSpy.deactivate.and.returnValue(of({ ...mockHoliday, active: false }));
    component.deactivateHoliday(1);
    expect(holidayServiceSpy.deactivate).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Holiday deactivated successfully');
  });

  it('should handle deactivate error', () => {
    holidayServiceSpy.deactivate.and.returnValue(throwError(() => new Error('fail')));
    component.deactivateHoliday(1);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to deactivate holiday');
  });

  it('should update pagination on page change', () => {
    component.onPageChange({ pageIndex: 2, pageSize: 25, length: 100 });
    expect(component.pageIndex).toBe(2);
    expect(component.pageSize).toBe(25);
  });

  it('should have required validators', () => {
    component.ngOnInit();
    expect(component.form.get('name')?.hasError('required')).toBeTrue();
    expect(component.form.get('date')?.hasError('required')).toBeTrue();
    expect(component.form.get('type')?.hasError('required')).toBeTrue();
  });
});
