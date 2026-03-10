import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';

import { RosterCalendarComponent } from './roster-calendar.component';
import { ShiftRosterService } from '../../../services/shift-roster.service';
import { EmployeeService } from '../../../services/employee.service';
import { NotificationService } from '../../../services/notification.service';
import { ShiftRosterResponse } from '../../../models/shift-roster.model';
import { Page } from '../../../models/page.model';
import { EmployeeResponse } from '../../../models/employee.model';

describe('RosterCalendarComponent', () => {
  let component: RosterCalendarComponent;
  let fixture: ComponentFixture<RosterCalendarComponent>;
  let shiftRosterServiceSpy: jasmine.SpyObj<ShiftRosterService>;
  let employeeServiceSpy: jasmine.SpyObj<EmployeeService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockEmployee: EmployeeResponse = {
    id: 1,
    employeeCode: 'EMP001',
    firstName: 'John',
    lastName: 'Doe',
  } as EmployeeResponse;

  const mockRoster: ShiftRosterResponse = {
    id: 10,
    employeeId: 1,
    employeeName: 'John Doe',
    employeeCode: 'EMP001',
    shiftId: 5,
    shiftName: 'Morning',
    effectiveDate: '2025-01-06',
    endDate: '2025-12-31',
    rotationPatternId: null,
    rotationPatternName: null,
    createdAt: '2025-01-01T00:00:00',
    updatedAt: '2025-01-01T00:00:00',
  };

  const mockPage: Page<EmployeeResponse> = {
    content: [mockEmployee],
    totalElements: 1,
    totalPages: 1,
    size: 1000,
    number: 0,
    first: true,
    last: true,
    empty: false,
  };

  beforeEach(async () => {
    shiftRosterServiceSpy = jasmine.createSpyObj('ShiftRosterService', ['getByDateRange']);
    employeeServiceSpy = jasmine.createSpyObj('EmployeeService', ['getAll']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    shiftRosterServiceSpy.getByDateRange.and.returnValue(of([mockRoster]));
    employeeServiceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [RosterCalendarComponent, NoopAnimationsModule],
      providers: [
        { provide: ShiftRosterService, useValue: shiftRosterServiceSpy },
        { provide: EmployeeService, useValue: employeeServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: Router, useValue: routerSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RosterCalendarComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load data on init', () => {
    fixture.detectChanges();
    expect(employeeServiceSpy.getAll).toHaveBeenCalledWith(0, 1000);
    expect(shiftRosterServiceSpy.getByDateRange).toHaveBeenCalled();
    expect(component.loading).toBeFalse();
  });

  it('should build 7 week days', () => {
    fixture.detectChanges();
    expect(component.weekDays.length).toBe(7);
    expect(component.weekDays[0].dayOfWeek).toBe('Mon');
    expect(component.weekDays[6].dayOfWeek).toBe('Sun');
  });

  it('should navigate to previous week', () => {
    fixture.detectChanges();
    const initialStart = new Date(component.weekStartDate);
    component.previousWeek();
    const expectedDate = new Date(initialStart);
    expectedDate.setDate(expectedDate.getDate() - 7);
    expect(component.weekStartDate.getTime()).toBe(expectedDate.getTime());
    expect(shiftRosterServiceSpy.getByDateRange).toHaveBeenCalledTimes(2);
  });

  it('should navigate to next week', () => {
    fixture.detectChanges();
    const initialStart = new Date(component.weekStartDate);
    component.nextWeek();
    const expectedDate = new Date(initialStart);
    expectedDate.setDate(expectedDate.getDate() + 7);
    expect(component.weekStartDate.getTime()).toBe(expectedDate.getTime());
    expect(shiftRosterServiceSpy.getByDateRange).toHaveBeenCalledTimes(2);
  });

  it('should navigate to current week on goToToday', () => {
    fixture.detectChanges();
    component.nextWeek();
    component.goToToday();
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const day = today.getDay();
    const expectedMonday = new Date(today);
    expectedMonday.setDate(today.getDate() - day + (day === 0 ? -6 : 1));
    expect(component.weekStartDate.getTime()).toBe(expectedMonday.getTime());
  });

  it('should return week label', () => {
    fixture.detectChanges();
    const label = component.getWeekLabel();
    expect(label).toBeTruthy();
    expect(label).toContain('–');
  });

  it('should navigate to edit on assigned cell click', () => {
    fixture.detectChanges();
    const row = component.employeeRows[0];
    if (row) {
      const assignedDay = component.weekDays.find((d) => component.getAssignment(row, d));
      if (assignedDay) {
        component.onCellClick(row, assignedDay);
        expect(routerSpy.navigate).toHaveBeenCalledWith(['/shift-rosters', mockRoster.id, 'edit']);
      }
    }
  });

  it('should navigate to new with query params on empty cell click', () => {
    shiftRosterServiceSpy.getByDateRange.and.returnValue(of([]));
    fixture.detectChanges();
    component.goToToday();
    const row = component.employeeRows[0];
    if (row) {
      component.onCellClick(row, component.weekDays[0]);
      expect(routerSpy.navigate).toHaveBeenCalledWith(
        ['/shift-rosters/new'],
        jasmine.objectContaining({
          queryParams: jasmine.objectContaining({
            employeeId: mockEmployee.id,
          }),
        })
      );
    }
  });

  it('should show error notification on load failure', () => {
    shiftRosterServiceSpy.getByDateRange.and.returnValue(throwError(() => new Error('fail')));
    fixture.detectChanges();
    component.goToToday();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load roster calendar data');
    expect(component.loading).toBeFalse();
  });
});
