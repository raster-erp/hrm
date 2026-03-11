import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { WfhRequestFormComponent } from './wfh-request-form.component';
import { WfhRequestService } from '../../../services/wfh-request.service';
import { NotificationService } from '../../../services/notification.service';
import { WfhRequestResponse } from '../../../models/wfh-request.model';

describe('WfhRequestFormComponent', () => {
  let component: WfhRequestFormComponent;
  let fixture: ComponentFixture<WfhRequestFormComponent>;
  let wfhRequestServiceSpy: jasmine.SpyObj<WfhRequestService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockRecord: WfhRequestResponse = {
    id: 1, employeeId: 100, employeeCode: 'EMP001', employeeName: 'John Doe',
    requestDate: '2026-01-15', reason: 'Personal work', status: 'PENDING',
    approvedBy: null, approvedAt: null, remarks: 'Test',
    createdAt: '2026-01-15T00:00:00', updatedAt: '2026-01-15T00:00:00'
  };

  function setup(routeId: string | null = null) {
    wfhRequestServiceSpy = jasmine.createSpyObj('WfhRequestService', ['getById', 'create', 'update']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [
        WfhRequestFormComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: WfhRequestService, useValue: wfhRequestServiceSpy },
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

    fixture = TestBed.createComponent(WfhRequestFormComponent);
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

    it('should require employeeId', () => {
      expect(component.recordForm.get('employeeId')?.hasError('required')).toBeTrue();
    });

    it('should require requestDate', () => {
      expect(component.recordForm.get('requestDate')?.hasError('required')).toBeTrue();
    });

    it('should require reason', () => {
      expect(component.recordForm.get('reason')?.hasError('required')).toBeTrue();
    });

    it('should enforce maxLength on reason', () => {
      component.recordForm.patchValue({ reason: 'a'.repeat(501) });
      expect(component.recordForm.get('reason')?.hasError('maxlength')).toBeTrue();
    });

    it('should enforce maxLength on remarks', () => {
      component.recordForm.patchValue({ remarks: 'a'.repeat(501) });
      expect(component.recordForm.get('remarks')?.hasError('maxlength')).toBeTrue();
    });

    it('should show error if submitting invalid form', () => {
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Please fill in all required fields correctly');
    });

    it('should create record on valid submit', () => {
      fillValidForm();
      wfhRequestServiceSpy.create.and.returnValue(of(mockRecord));
      component.onSubmit();
      expect(wfhRequestServiceSpy.create).toHaveBeenCalled();
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('WFH request created successfully');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/wfh-requests']);
    });

    it('should handle create error', () => {
      fillValidForm();
      wfhRequestServiceSpy.create.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to create WFH request');
    });

    it('should navigate back on cancel', () => {
      component.onCancel();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/wfh-requests']);
    });
  });

  describe('edit mode', () => {
    beforeEach(() => {
      wfhRequestServiceSpy = jasmine.createSpyObj('WfhRequestService', ['getById', 'create', 'update']);
      notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
      routerSpy = jasmine.createSpyObj('Router', ['navigate']);

      wfhRequestServiceSpy.getById.and.returnValue(of(mockRecord));

      TestBed.configureTestingModule({
        imports: [WfhRequestFormComponent, HttpClientTestingModule, NoopAnimationsModule],
        providers: [
          { provide: WfhRequestService, useValue: wfhRequestServiceSpy },
          { provide: NotificationService, useValue: notificationServiceSpy },
          { provide: Router, useValue: routerSpy },
          { provide: ActivatedRoute, useValue: { params: of({}), snapshot: { paramMap: { get: () => '1' } } } }
        ]
      });

      fixture = TestBed.createComponent(WfhRequestFormComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should be in edit mode', () => {
      expect(component.isEditMode).toBeTrue();
      expect(component.recordId).toBe(1);
    });

    it('should load record data', () => {
      expect(wfhRequestServiceSpy.getById).toHaveBeenCalledWith(1);
      expect(component.recordForm.get('employeeId')?.value).toBe(100);
    });

    it('should update record on submit', () => {
      wfhRequestServiceSpy.update.and.returnValue(of(mockRecord));
      component.onSubmit();
      expect(wfhRequestServiceSpy.update).toHaveBeenCalledWith(1, jasmine.any(Object));
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('WFH request updated successfully');
    });

    it('should handle update error', () => {
      wfhRequestServiceSpy.update.and.returnValue(throwError(() => new Error('fail')));
      component.onSubmit();
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to update WFH request');
    });

    it('should handle load record error', () => {
      wfhRequestServiceSpy.getById.and.returnValue(throwError(() => new Error('fail')));
      component.loadRecord(999);
      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load WFH request');
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/wfh-requests']);
    });
  });

  function fillValidForm() {
    component.recordForm.patchValue({
      employeeId: 100,
      requestDate: '2026-01-15',
      reason: 'Personal work',
      remarks: 'Test'
    });
  }
});
