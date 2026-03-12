import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { TeamAvailabilityComponent } from './team-availability.component';
import { LeaveCalendarService } from '../../../services/leave-calendar.service';
import { NotificationService } from '../../../services/notification.service';
import { DepartmentService } from '../../../services/department.service';
import { TeamAvailabilityResponse } from '../../../models/leave-calendar.model';

describe('TeamAvailabilityComponent', () => {
  let component: TeamAvailabilityComponent;
  let fixture: ComponentFixture<TeamAvailabilityComponent>;
  let leaveCalendarServiceSpy: jasmine.SpyObj<LeaveCalendarService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let departmentServiceSpy: jasmine.SpyObj<DepartmentService>;

  const mockAvailability: TeamAvailabilityResponse[] = [
    {
      date: '2026-03-10', totalMembers: 10, availableMembers: 8,
      onLeave: 1, onPlannedLeave: 1, coveragePercentage: 80,
      absentEmployees: ['John Doe', 'Jane Smith']
    }
  ];

  beforeEach(async () => {
    leaveCalendarServiceSpy = jasmine.createSpyObj('LeaveCalendarService',
      ['getCalendarEntries', 'getTeamAvailability']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    departmentServiceSpy = jasmine.createSpyObj('DepartmentService', ['getAll']);

    departmentServiceSpy.getAll.and.returnValue(of([]));
    leaveCalendarServiceSpy.getTeamAvailability.and.returnValue(of(mockAvailability));

    await TestBed.configureTestingModule({
      imports: [
        TeamAvailabilityComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: LeaveCalendarService, useValue: leaveCalendarServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: DepartmentService, useValue: departmentServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TeamAvailabilityComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load departments on init', () => {
    component.ngOnInit();
    expect(departmentServiceSpy.getAll).toHaveBeenCalled();
  });

  it('should set default dates on init', () => {
    component.ngOnInit();
    expect(component.startDate).toBeTruthy();
    expect(component.endDate).toBeTruthy();
  });

  it('should require department and dates to search', () => {
    component.selectedDepartmentId = null;
    component.loadAvailability();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Please select a department and date range');
  });

  it('should load availability with valid parameters', () => {
    component.selectedDepartmentId = 1;
    component.startDate = '2026-03-01';
    component.endDate = '2026-03-31';
    component.loadAvailability();
    expect(leaveCalendarServiceSpy.getTeamAvailability).toHaveBeenCalledWith(1, '2026-03-01', '2026-03-31');
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle availability load error', () => {
    leaveCalendarServiceSpy.getTeamAvailability.and.returnValue(throwError(() => new Error('fail')));
    component.selectedDepartmentId = 1;
    component.startDate = '2026-03-01';
    component.endDate = '2026-03-31';
    component.loadAvailability();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load team availability');
  });

  it('should return correct coverage class for good coverage', () => {
    expect(component.getCoverageClass(85)).toBe('coverage-good');
  });

  it('should return correct coverage class for warning coverage', () => {
    expect(component.getCoverageClass(60)).toBe('coverage-warning');
  });

  it('should return correct coverage class for critical coverage', () => {
    expect(component.getCoverageClass(30)).toBe('coverage-critical');
  });

  it('should have displayed columns defined', () => {
    expect(component.displayedColumns.length).toBeGreaterThan(0);
  });
});
