import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { LeaveCalendarViewComponent } from './leave-calendar-view.component';
import { LeaveCalendarService } from '../../../services/leave-calendar.service';
import { NotificationService } from '../../../services/notification.service';
import { DepartmentService } from '../../../services/department.service';
import { LeaveCalendarEntry } from '../../../models/leave-calendar.model';

describe('LeaveCalendarViewComponent', () => {
  let component: LeaveCalendarViewComponent;
  let fixture: ComponentFixture<LeaveCalendarViewComponent>;
  let leaveCalendarServiceSpy: jasmine.SpyObj<LeaveCalendarService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let departmentServiceSpy: jasmine.SpyObj<DepartmentService>;

  const mockEntries: LeaveCalendarEntry[] = [
    {
      id: 1, type: 'HOLIDAY', employeeId: null, employeeName: null,
      leaveTypeName: 'Republic Day', leaveTypeCategory: null,
      fromDate: '2026-01-26', toDate: '2026-01-26', status: 'ACTIVE', color: '#4caf50'
    },
    {
      id: 2, type: 'LEAVE', employeeId: 100, employeeName: 'John Doe',
      leaveTypeName: 'Annual Leave', leaveTypeCategory: 'PAID',
      fromDate: '2026-03-10', toDate: '2026-03-12', status: 'APPROVED', color: '#2196f3'
    }
  ];

  beforeEach(async () => {
    leaveCalendarServiceSpy = jasmine.createSpyObj('LeaveCalendarService',
      ['getCalendarEntries', 'getTeamAvailability']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    departmentServiceSpy = jasmine.createSpyObj('DepartmentService', ['getAll']);

    leaveCalendarServiceSpy.getCalendarEntries.and.returnValue(of(mockEntries));
    departmentServiceSpy.getAll.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [
        LeaveCalendarViewComponent,
        HttpClientTestingModule,
        NoopAnimationsModule,
        RouterTestingModule
      ],
      providers: [
        { provide: LeaveCalendarService, useValue: leaveCalendarServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: DepartmentService, useValue: departmentServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LeaveCalendarViewComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load calendar on init', () => {
    component.ngOnInit();
    expect(leaveCalendarServiceSpy.getCalendarEntries).toHaveBeenCalled();
    expect(component.entries.length).toBe(2);
  });

  it('should load departments on init', () => {
    component.ngOnInit();
    expect(departmentServiceSpy.getAll).toHaveBeenCalled();
  });

  it('should handle calendar load error', () => {
    leaveCalendarServiceSpy.getCalendarEntries.and.returnValue(throwError(() => new Error('fail')));
    component.loadCalendar();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load calendar entries');
  });

  it('should build calendar with 42 days', () => {
    component.ngOnInit();
    expect(component.calendarDays.length).toBe(42);
  });

  it('should navigate to previous month', () => {
    const initialMonth = component.currentDate.getMonth();
    component.previousMonth();
    const expectedMonth = initialMonth === 0 ? 11 : initialMonth - 1;
    expect(component.currentDate.getMonth()).toBe(expectedMonth);
  });

  it('should navigate to next month', () => {
    const initialMonth = component.currentDate.getMonth();
    component.nextMonth();
    const expectedMonth = initialMonth === 11 ? 0 : initialMonth + 1;
    expect(component.currentDate.getMonth()).toBe(expectedMonth);
  });

  it('should go to today', () => {
    component.currentDate = new Date(2025, 0, 1);
    component.goToToday();
    expect(component.currentDate.toDateString()).toBe(new Date().toDateString());
  });

  it('should clear filters', () => {
    component.selectedDepartmentId = 1;
    component.selectedEmployeeId = 100;
    component.clearFilters();
    expect(component.selectedDepartmentId).toBeNull();
    expect(component.selectedEmployeeId).toBeNull();
  });

  it('should generate current month label', () => {
    component.currentDate = new Date(2026, 2, 1); // March 2026
    expect(component.currentMonthLabel).toBe('March 2026');
  });

  it('should return holiday tooltip', () => {
    const tooltip = component.getEntryTooltip(mockEntries[0]);
    expect(tooltip).toBe('Holiday: Republic Day');
  });

  it('should return leave tooltip', () => {
    const tooltip = component.getEntryTooltip(mockEntries[1]);
    expect(tooltip).toBe('John Doe: Annual Leave (APPROVED)');
  });

  it('should have week days defined', () => {
    expect(component.weekDays.length).toBe(7);
  });
});
