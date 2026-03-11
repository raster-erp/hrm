import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LeaveApplicationFormComponent } from './leave-application-form.component';
import { LeaveApplicationService } from '../../../services/leave-application.service';
import { NotificationService } from '../../../services/notification.service';
import { LeaveApplicationResponse } from '../../../models/leave-application.model';

describe('LeaveApplicationFormComponent', () => {
  let component: LeaveApplicationFormComponent;
  let fixture: ComponentFixture<LeaveApplicationFormComponent>;
  let leaveApplicationServiceSpy: jasmine.SpyObj<LeaveApplicationService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  const mockRecord: LeaveApplicationResponse = {
    id: 1, employeeId: 100, employeeCode: 'EMP001', employeeName: 'John Doe',
    leaveTypeId: 1, leaveTypeName: 'Casual Leave',
    fromDate: '2026-01-15', toDate: '2026-01-17', numberOfDays: 3,
    reason: 'Personal work', status: 'PENDING', approvalLevel: 1,
    remarks: null, approvedBy: null, approvedAt: null,
    createdAt: '2026-01-10T00:00:00', updatedAt: '2026-01-10T00:00:00'
  };

  function createComponent(paramId: string | null = null) {
    leaveApplicationServiceSpy = jasmine.createSpyObj('LeaveApplicationService',
      ['getById', 'create', 'update']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    TestBed.configureTestingModule({
      imports: [
        LeaveApplicationFormComponent,
        HttpClientTestingModule,
        NoopAnimationsModule,
        RouterTestingModule
      ],
      providers: [
        { provide: LeaveApplicationService, useValue: leaveApplicationServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => paramId } } }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LeaveApplicationFormComponent);
    component = fixture.componentInstance;
  }

  describe('Create mode', () => {
    beforeEach(() => {
      createComponent(null);
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize form on init', () => {
      component.ngOnInit();
      expect(component.form).toBeTruthy();
      expect(component.isEdit).toBeFalse();
    });

    it('should have required validators', () => {
      component.ngOnInit();
      expect(component.form.get('employeeId')?.hasError('required')).toBeTrue();
      expect(component.form.get('leaveTypeId')?.hasError('required')).toBeTrue();
      expect(component.form.get('fromDate')?.hasError('required')).toBeTrue();
      expect(component.form.get('toDate')?.hasError('required')).toBeTrue();
      expect(component.form.get('numberOfDays')?.hasError('required')).toBeTrue();
    });

    it('should not submit invalid form', () => {
      component.ngOnInit();
      component.onSubmit();
      expect(leaveApplicationServiceSpy.create).not.toHaveBeenCalled();
    });

    it('should submit valid form', () => {
      leaveApplicationServiceSpy.create.and.returnValue(of(mockRecord));
      component.ngOnInit();
      component.form.patchValue({
        employeeId: 100,
        leaveTypeId: 1,
        fromDate: '2026-01-15',
        toDate: '2026-01-17',
        numberOfDays: 3
      });
      component.onSubmit();
      expect(leaveApplicationServiceSpy.create).toHaveBeenCalled();
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Leave application submitted successfully');
    });

    it('should handle create error', () => {
      leaveApplicationServiceSpy.create.and.returnValue(throwError(() => new Error('fail')));
      component.ngOnInit();
      component.form.patchValue({
        employeeId: 100,
        leaveTypeId: 1,
        fromDate: '2026-01-15',
        toDate: '2026-01-17',
        numberOfDays: 3
      });
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to submit leave application');
    });
  });

  describe('Edit mode', () => {
    beforeEach(() => {
      createComponent('1');
      leaveApplicationServiceSpy.getById.and.returnValue(of(mockRecord));
    });

    it('should be in edit mode', () => {
      component.ngOnInit();
      expect(component.isEdit).toBeTrue();
      expect(component.recordId).toBe(1);
    });

    it('should load record on init', () => {
      component.ngOnInit();
      expect(leaveApplicationServiceSpy.getById).toHaveBeenCalledWith(1);
    });

    it('should populate form with record data', () => {
      component.ngOnInit();
      expect(component.form.get('employeeId')?.value).toBe(100);
      expect(component.form.get('leaveTypeId')?.value).toBe(1);
    });

    it('should handle load error', () => {
      leaveApplicationServiceSpy.getById.and.returnValue(throwError(() => new Error('fail')));
      component.ngOnInit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load leave application');
    });

    it('should call update on submit', () => {
      leaveApplicationServiceSpy.update.and.returnValue(of(mockRecord));
      component.ngOnInit();
      component.form.patchValue({
        reason: 'Updated reason',
        numberOfDays: 3
      });
      component.onSubmit();
      expect(leaveApplicationServiceSpy.update).toHaveBeenCalled();
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Leave application updated successfully');
    });

    it('should handle update error', () => {
      leaveApplicationServiceSpy.update.and.returnValue(throwError(() => new Error('fail')));
      component.ngOnInit();
      component.form.patchValue({
        reason: 'Updated reason',
        numberOfDays: 3
      });
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to update leave application');
    });

    it('should disable form if status is not PENDING', () => {
      const approvedRecord = { ...mockRecord, status: 'APPROVED' };
      leaveApplicationServiceSpy.getById.and.returnValue(of(approvedRecord));
      component.ngOnInit();
      expect(component.form.disabled).toBeTrue();
    });
  });
});
