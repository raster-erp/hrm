import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { WfhCheckinComponent } from './wfh-checkin.component';
import { WfhRequestService } from '../../../services/wfh-request.service';
import { NotificationService } from '../../../services/notification.service';
import { WfhCheckInResponse } from '../../../models/wfh-request.model';

describe('WfhCheckinComponent', () => {
  let component: WfhCheckinComponent;
  let fixture: ComponentFixture<WfhCheckinComponent>;
  let wfhRequestServiceSpy: jasmine.SpyObj<WfhRequestService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  const mockSession: WfhCheckInResponse = {
    id: 1, wfhRequestId: 10, employeeId: 100, employeeCode: 'EMP001',
    employeeName: 'John Doe', checkInTime: '2026-01-15T09:00:00',
    checkOutTime: null, ipAddress: '192.168.1.1', location: 'Home',
    createdAt: '2026-01-15T09:00:00', updatedAt: '2026-01-15T09:00:00'
  };

  const mockLogs: WfhCheckInResponse[] = [
    mockSession,
    {
      id: 2, wfhRequestId: 10, employeeId: 100, employeeCode: 'EMP001',
      employeeName: 'John Doe', checkInTime: '2026-01-14T09:00:00',
      checkOutTime: '2026-01-14T17:00:00', ipAddress: '192.168.1.1', location: 'Home',
      createdAt: '2026-01-14T09:00:00', updatedAt: '2026-01-14T17:00:00'
    }
  ];

  beforeEach(async () => {
    wfhRequestServiceSpy = jasmine.createSpyObj('WfhRequestService',
      ['getActiveSession', 'getActivityLogs', 'checkIn', 'checkOut']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    await TestBed.configureTestingModule({
      imports: [
        WfhCheckinComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: WfhRequestService, useValue: wfhRequestServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(WfhCheckinComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show error when loading status without request ID', () => {
    component.wfhRequestId = null;
    component.loadStatus();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Please enter a WFH Request ID');
  });

  it('should load active session and activity logs', () => {
    wfhRequestServiceSpy.getActiveSession.and.returnValue(of(mockSession));
    wfhRequestServiceSpy.getActivityLogs.and.returnValue(of(mockLogs));

    component.wfhRequestId = 10;
    component.loadStatus();

    expect(wfhRequestServiceSpy.getActiveSession).toHaveBeenCalledWith(10);
    expect(wfhRequestServiceSpy.getActivityLogs).toHaveBeenCalledWith(10);
    expect(component.activeSession).toEqual(mockSession);
    expect(component.activityLogs.data.length).toBe(2);
  });

  it('should handle active session load error gracefully', () => {
    wfhRequestServiceSpy.getActiveSession.and.returnValue(throwError(() => new Error('fail')));
    wfhRequestServiceSpy.getActivityLogs.and.returnValue(of(mockLogs));

    component.wfhRequestId = 10;
    component.loadStatus();

    expect(component.activeSession).toBeNull();
    expect(component.loading).toBe(false);
  });

  it('should handle activity logs load error', () => {
    wfhRequestServiceSpy.getActiveSession.and.returnValue(of(mockSession));
    wfhRequestServiceSpy.getActivityLogs.and.returnValue(throwError(() => new Error('fail')));

    component.wfhRequestId = 10;
    component.loadStatus();

    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load activity logs');
  });

  it('should show error when checking in without request ID', () => {
    component.wfhRequestId = null;
    component.checkIn();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Please enter a WFH Request ID');
  });

  it('should check in successfully', () => {
    wfhRequestServiceSpy.checkIn.and.returnValue(of(mockSession));
    wfhRequestServiceSpy.getActiveSession.and.returnValue(of(mockSession));
    wfhRequestServiceSpy.getActivityLogs.and.returnValue(of(mockLogs));

    component.wfhRequestId = 10;
    component.ipAddress = '192.168.1.1';
    component.location = 'Home';
    component.checkIn();

    expect(wfhRequestServiceSpy.checkIn).toHaveBeenCalledWith({
      wfhRequestId: 10, ipAddress: '192.168.1.1', location: 'Home'
    });
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Checked in successfully');
  });

  it('should handle check in error', () => {
    wfhRequestServiceSpy.checkIn.and.returnValue(throwError(() => new Error('fail')));

    component.wfhRequestId = 10;
    component.checkIn();

    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to check in');
    expect(component.checking).toBe(false);
  });

  it('should not check out without active session', () => {
    component.activeSession = null;
    component.checkOut();
    expect(wfhRequestServiceSpy.checkOut).not.toHaveBeenCalled();
  });

  it('should check out successfully', () => {
    wfhRequestServiceSpy.checkOut.and.returnValue(of(mockSession));
    wfhRequestServiceSpy.getActiveSession.and.returnValue(throwError(() => new Error('no session')));
    wfhRequestServiceSpy.getActivityLogs.and.returnValue(of(mockLogs));

    component.activeSession = mockSession;
    component.wfhRequestId = 10;
    component.checkOut();

    expect(wfhRequestServiceSpy.checkOut).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Checked out successfully');
  });

  it('should handle check out error', () => {
    wfhRequestServiceSpy.checkOut.and.returnValue(throwError(() => new Error('fail')));

    component.activeSession = mockSession;
    component.checkOut();

    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to check out');
    expect(component.checking).toBe(false);
  });

  it('should send null for empty ip and location on check in', () => {
    wfhRequestServiceSpy.checkIn.and.returnValue(of(mockSession));
    wfhRequestServiceSpy.getActiveSession.and.returnValue(of(mockSession));
    wfhRequestServiceSpy.getActivityLogs.and.returnValue(of(mockLogs));

    component.wfhRequestId = 10;
    component.ipAddress = '';
    component.location = '';
    component.checkIn();

    expect(wfhRequestServiceSpy.checkIn).toHaveBeenCalledWith({
      wfhRequestId: 10, ipAddress: null, location: null
    });
  });
});
