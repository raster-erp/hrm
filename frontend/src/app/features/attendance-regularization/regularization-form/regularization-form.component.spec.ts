import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { RegularizationFormComponent } from './regularization-form.component';
import { RegularizationRequestService } from '../../../services/regularization-request.service';
import { NotificationService } from '../../../services/notification.service';
import { RegularizationRequestResponse } from '../../../models/regularization-request.model';

describe('RegularizationFormComponent', () => {
  let component: RegularizationFormComponent;
  let fixture: ComponentFixture<RegularizationFormComponent>;
  let regularizationServiceSpy: jasmine.SpyObj<RegularizationRequestService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  const mockRecord: RegularizationRequestResponse = {
    id: 1, employeeId: 100, employeeCode: 'EMP001', employeeName: 'John Doe',
    requestDate: '2026-01-15', type: 'MISSED_PUNCH', reason: 'Forgot to punch',
    originalPunchIn: null, originalPunchOut: null,
    correctedPunchIn: '2026-01-15T09:00:00', correctedPunchOut: '2026-01-15T18:00:00',
    status: 'PENDING', approvalLevel: 1, remarks: null,
    approvedBy: null, approvedAt: null,
    createdAt: '2026-01-15T00:00:00', updatedAt: '2026-01-15T00:00:00'
  };

  function createComponent(paramId: string | null = null) {
    regularizationServiceSpy = jasmine.createSpyObj('RegularizationRequestService',
      ['getById', 'create', 'update']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    TestBed.configureTestingModule({
      imports: [
        RegularizationFormComponent,
        HttpClientTestingModule,
        NoopAnimationsModule,
        RouterTestingModule
      ],
      providers: [
        { provide: RegularizationRequestService, useValue: regularizationServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => paramId } } }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegularizationFormComponent);
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
      expect(component.form.get('requestDate')?.hasError('required')).toBeTrue();
      expect(component.form.get('type')?.hasError('required')).toBeTrue();
      expect(component.form.get('reason')?.hasError('required')).toBeTrue();
      expect(component.form.get('correctedPunchIn')?.hasError('required')).toBeTrue();
      expect(component.form.get('correctedPunchOut')?.hasError('required')).toBeTrue();
    });

    it('should not submit invalid form', () => {
      component.ngOnInit();
      component.onSubmit();
      expect(regularizationServiceSpy.create).not.toHaveBeenCalled();
    });

    it('should submit valid form', () => {
      regularizationServiceSpy.create.and.returnValue(of(mockRecord));
      component.ngOnInit();
      component.form.patchValue({
        employeeId: 100,
        requestDate: '2026-01-15',
        type: 'MISSED_PUNCH',
        reason: 'Forgot to punch',
        correctedPunchIn: '2026-01-15T09:00:00',
        correctedPunchOut: '2026-01-15T18:00:00'
      });
      component.onSubmit();
      expect(regularizationServiceSpy.create).toHaveBeenCalled();
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Regularization request submitted successfully');
    });

    it('should handle create error', () => {
      regularizationServiceSpy.create.and.returnValue(throwError(() => new Error('fail')));
      component.ngOnInit();
      component.form.patchValue({
        employeeId: 100,
        requestDate: '2026-01-15',
        type: 'MISSED_PUNCH',
        reason: 'Forgot to punch',
        correctedPunchIn: '2026-01-15T09:00:00',
        correctedPunchOut: '2026-01-15T18:00:00'
      });
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to submit regularization request');
    });

    it('should format type correctly', () => {
      expect(component.formatType('MISSED_PUNCH')).toBe('Missed Punch');
      expect(component.formatType('ON_DUTY')).toBe('On Duty');
      expect(component.formatType('CLIENT_VISIT')).toBe('Client Visit');
      expect(component.formatType('UNKNOWN')).toBe('UNKNOWN');
    });
  });

  describe('Edit mode', () => {
    beforeEach(() => {
      createComponent('1');
      regularizationServiceSpy.getById.and.returnValue(of(mockRecord));
    });

    it('should be in edit mode', () => {
      component.ngOnInit();
      expect(component.isEdit).toBeTrue();
      expect(component.recordId).toBe(1);
    });

    it('should load record on init', () => {
      component.ngOnInit();
      expect(regularizationServiceSpy.getById).toHaveBeenCalledWith(1);
    });

    it('should populate form with record data', () => {
      component.ngOnInit();
      expect(component.form.get('employeeId')?.value).toBe(100);
      expect(component.form.get('type')?.value).toBe('MISSED_PUNCH');
    });

    it('should handle load error', () => {
      regularizationServiceSpy.getById.and.returnValue(throwError(() => new Error('fail')));
      component.ngOnInit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load regularization request');
    });

    it('should call update on submit', () => {
      regularizationServiceSpy.update.and.returnValue(of(mockRecord));
      component.ngOnInit();
      component.form.patchValue({
        reason: 'Updated reason',
        correctedPunchIn: '2026-01-15T09:00:00',
        correctedPunchOut: '2026-01-15T18:00:00'
      });
      component.onSubmit();
      expect(regularizationServiceSpy.update).toHaveBeenCalled();
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Regularization request updated successfully');
    });

    it('should handle update error', () => {
      regularizationServiceSpy.update.and.returnValue(throwError(() => new Error('fail')));
      component.ngOnInit();
      component.form.patchValue({
        reason: 'Updated reason',
        correctedPunchIn: '2026-01-15T09:00:00',
        correctedPunchOut: '2026-01-15T18:00:00'
      });
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to update regularization request');
    });

    it('should disable form if status is not PENDING', () => {
      const approvedRecord = { ...mockRecord, status: 'APPROVED' };
      regularizationServiceSpy.getById.and.returnValue(of(approvedRecord));
      component.ngOnInit();
      expect(component.form.disabled).toBeTrue();
    });
  });
});
