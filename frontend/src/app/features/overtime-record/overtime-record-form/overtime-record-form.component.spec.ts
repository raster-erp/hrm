import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { OvertimeRecordFormComponent } from './overtime-record-form.component';
import { OvertimeRecordService } from '../../../services/overtime-record.service';
import { OvertimePolicyService } from '../../../services/overtime-policy.service';
import { NotificationService } from '../../../services/notification.service';
import { OvertimeRecordResponse } from '../../../models/overtime-record.model';
import { OvertimePolicyResponse } from '../../../models/overtime-policy.model';

describe('OvertimeRecordFormComponent', () => {
  let component: OvertimeRecordFormComponent;
  let fixture: ComponentFixture<OvertimeRecordFormComponent>;
  let overtimeRecordServiceSpy: jasmine.SpyObj<OvertimeRecordService>;
  let overtimePolicyServiceSpy: jasmine.SpyObj<OvertimePolicyService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockRecord: OvertimeRecordResponse = {
    id: 1, employeeId: 100, employeeCode: 'EMP001', employeeName: 'John Doe',
    overtimeDate: '2026-01-15', overtimePolicyId: 1, overtimePolicyName: 'Standard OT',
    overtimePolicyType: 'REGULAR', overtimeMinutes: 60, status: 'PENDING',
    source: 'MANUAL', shiftStartTime: '09:00', shiftEndTime: '17:00',
    actualStartTime: null, actualEndTime: null, remarks: 'Test',
    approvedBy: null, approvedAt: null,
    createdAt: '2026-01-15T00:00:00', updatedAt: '2026-01-15T00:00:00'
  };

  const mockPolicies: OvertimePolicyResponse[] = [
    {
      id: 1, name: 'Standard OT', type: 'REGULAR', rateMultiplier: 1.5,
      minOvertimeMinutes: 30, maxOvertimeMinutesPerDay: 240,
      maxOvertimeMinutesPerMonth: null, requiresApproval: true,
      active: true, description: 'Standard overtime',
      createdAt: '2026-01-01T00:00:00', updatedAt: '2026-01-01T00:00:00'
    }
  ];

  function setup(routeId: string | null = null) {
    overtimeRecordServiceSpy = jasmine.createSpyObj('OvertimeRecordService', ['getById', 'create', 'update']);
    overtimePolicyServiceSpy = jasmine.createSpyObj('OvertimePolicyService', ['getActive']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    overtimePolicyServiceSpy.getActive.and.returnValue(of(mockPolicies));

    TestBed.configureTestingModule({
      imports: [
        OvertimeRecordFormComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: OvertimeRecordService, useValue: overtimeRecordServiceSpy },
        { provide: OvertimePolicyService, useValue: overtimePolicyServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: Router, useValue: routerSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({}),
            snapshot: { paramMap: { get: () => routeId } }
          }
        }
      ]
    });

    fixture = TestBed.createComponent(OvertimeRecordFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  describe('create mode', () => {
    beforeEach(() => setup(null));

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should be in create mode by default', () => {
      expect(component.isEditMode).toBeFalse();
    });

    it('should initialize form group', () => {
      expect(component.recordForm).toBeDefined();
    });

    it('should load active policies', () => {
      expect(overtimePolicyServiceSpy.getActive).toHaveBeenCalled();
      expect(component.activePolicies.length).toBe(1);
    });

    it('should handle policy load error', () => {
      overtimePolicyServiceSpy.getActive.and.returnValue(throwError(() => new Error('fail')));
      component.loadActivePolicies();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load overtime policies');
    });

    it('should require employeeId', () => {
      expect(component.recordForm.get('employeeId')?.hasError('required')).toBeTrue();
    });

    it('should require overtimeDate', () => {
      expect(component.recordForm.get('overtimeDate')?.hasError('required')).toBeTrue();
    });

    it('should require overtimePolicyId', () => {
      expect(component.recordForm.get('overtimePolicyId')?.hasError('required')).toBeTrue();
    });

    it('should require overtimeMinutes', () => {
      expect(component.recordForm.get('overtimeMinutes')?.hasError('required')).toBeTrue();
    });

    it('should enforce minimum of 1 for overtimeMinutes', () => {
      component.recordForm.patchValue({ overtimeMinutes: 0 });
      expect(component.recordForm.get('overtimeMinutes')?.hasError('min')).toBeTrue();
    });

    it('should show error if submitting invalid form', () => {
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Please fill in all required fields correctly');
    });

    it('should create record on valid submit', () => {
      fillValidForm();
      overtimeRecordServiceSpy.create.and.returnValue(of(mockRecord));
      component.onSubmit();
      expect(overtimeRecordServiceSpy.create).toHaveBeenCalled();
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Overtime record created successfully');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/overtime-records']);
    });

    it('should handle create error', () => {
      fillValidForm();
      overtimeRecordServiceSpy.create.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to create overtime record');
    });

    it('should navigate back on cancel', () => {
      component.onCancel();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/overtime-records']);
    });
  });

  describe('edit mode', () => {
    beforeEach(() => {
      overtimeRecordServiceSpy = jasmine.createSpyObj('OvertimeRecordService', ['getById', 'create', 'update']);
      overtimePolicyServiceSpy = jasmine.createSpyObj('OvertimePolicyService', ['getActive']);
      notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
      routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      overtimeRecordServiceSpy.getById.and.returnValue(of(mockRecord));
      overtimePolicyServiceSpy.getActive.and.returnValue(of(mockPolicies));

      TestBed.configureTestingModule({
        imports: [OvertimeRecordFormComponent, HttpClientTestingModule, NoopAnimationsModule],
        providers: [
          { provide: OvertimeRecordService, useValue: overtimeRecordServiceSpy },
          { provide: OvertimePolicyService, useValue: overtimePolicyServiceSpy },
          { provide: NotificationService, useValue: notificationServiceSpy },
          { provide: Router, useValue: routerSpy },
          { provide: ActivatedRoute, useValue: { params: of({}), snapshot: { paramMap: { get: () => '1' } } } }
        ]
      });

      fixture = TestBed.createComponent(OvertimeRecordFormComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should be in edit mode', () => {
      expect(component.isEditMode).toBeTrue();
      expect(component.recordId).toBe(1);
    });

    it('should load record data', () => {
      expect(overtimeRecordServiceSpy.getById).toHaveBeenCalledWith(1);
      expect(component.recordForm.get('employeeId')?.value).toBe(100);
    });

    it('should update record on submit', () => {
      overtimeRecordServiceSpy.update.and.returnValue(of(mockRecord));
      component.onSubmit();
      expect(overtimeRecordServiceSpy.update).toHaveBeenCalledWith(1, jasmine.any(Object));
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Overtime record updated successfully');
    });

    it('should handle update error', () => {
      overtimeRecordServiceSpy.update.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to update overtime record');
    });

    it('should handle load record error', () => {
      overtimeRecordServiceSpy.getById.and.returnValue(throwError(() => new Error('fail')));
      component.loadRecord(999);
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load overtime record');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/overtime-records']);
    });
  });

  function fillValidForm() {
    component.recordForm.patchValue({
      employeeId: 100,
      overtimeDate: '2026-01-15',
      overtimePolicyId: 1,
      overtimeMinutes: 60,
      shiftStartTime: '09:00',
      shiftEndTime: '17:00',
      remarks: 'Test'
    });
  }
});
