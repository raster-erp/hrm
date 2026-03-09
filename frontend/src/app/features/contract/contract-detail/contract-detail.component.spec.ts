import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ContractDetailComponent } from './contract-detail.component';
import { ContractService } from '../../../services/contract.service';
import { NotificationService } from '../../../services/notification.service';
import { ContractResponse, ContractAmendmentResponse } from '../../../models/contract.model';

describe('ContractDetailComponent', () => {
  let component: ContractDetailComponent;
  let fixture: ComponentFixture<ContractDetailComponent>;
  let contractServiceSpy: jasmine.SpyObj<ContractService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockContract: ContractResponse = {
    id: 1, employeeId: 1, employeeCode: 'EMP001', employeeName: 'John Doe',
    contractType: 'PERMANENT', startDate: '2023-01-01', endDate: '2024-01-01',
    terms: 'Standard terms', status: 'ACTIVE', createdAt: '2023-01-01T00:00:00', updatedAt: '2023-01-01T00:00:00'
  };

  const mockAmendments: ContractAmendmentResponse[] = [
    {
      id: 1, contractId: 1, amendmentDate: '2023-06-01',
      description: 'Salary revision', oldTerms: 'Old salary', newTerms: 'New salary',
      createdAt: '2023-06-01T00:00:00'
    }
  ];

  beforeEach(async () => {
    contractServiceSpy = jasmine.createSpyObj('ContractService', [
      'getById', 'getAmendments', 'addAmendment', 'renew'
    ]);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    contractServiceSpy.getById.and.returnValue(of(mockContract));
    contractServiceSpy.getAmendments.and.returnValue(of(mockAmendments));

    await TestBed.configureTestingModule({
      imports: [
        ContractDetailComponent,
        NoopAnimationsModule
      ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ContractService, useValue: contractServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: Router, useValue: routerSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({ id: '1' }),
            snapshot: { paramMap: { get: () => '1' } }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ContractDetailComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load contract on init', () => {
    component.ngOnInit();
    expect(contractServiceSpy.getById).toHaveBeenCalledWith(1);
    expect(component.contract).toEqual(mockContract);
    expect(component.loading).toBeFalse();
  });

  it('should load amendments on init', () => {
    component.ngOnInit();
    expect(contractServiceSpy.getAmendments).toHaveBeenCalledWith(1);
    expect(component.amendments.length).toBe(1);
    expect(component.amendmentsLoading).toBeFalse();
  });

  it('should handle contract load error', () => {
    contractServiceSpy.getById.and.returnValue(throwError(() => new Error('fail')));
    component.loadContract(1);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load contract details');
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/contracts']);
  });

  it('should handle amendments load error', () => {
    contractServiceSpy.getAmendments.and.returnValue(throwError(() => new Error('fail')));
    component.loadAmendments(1);
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load amendments');
  });

  it('should navigate to edit page', () => {
    component.contract = mockContract;
    component.editContract();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/contracts', 1, 'edit']);
  });

  it('should not navigate to edit if no contract', () => {
    component.contract = null;
    component.editContract();
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });

  it('should navigate back to list', () => {
    component.goBack();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/contracts']);
  });

  it('should toggle amendment form', () => {
    expect(component.showAmendmentForm).toBeFalse();
    component.toggleAmendmentForm();
    expect(component.showAmendmentForm).toBeTrue();
    component.toggleAmendmentForm();
    expect(component.showAmendmentForm).toBeFalse();
  });

  it('should submit amendment successfully', () => {
    component.contract = mockContract;
    component.ngOnInit();
    component.showAmendmentForm = true;

    component.amendmentForm.patchValue({
      amendmentDate: '2023-06-01',
      description: 'Test amendment',
      oldTerms: 'Old',
      newTerms: 'New'
    });

    const mockAmendment: ContractAmendmentResponse = {
      id: 2, contractId: 1, amendmentDate: '2023-06-01',
      description: 'Test amendment', oldTerms: 'Old', newTerms: 'New',
      createdAt: '2023-06-01T00:00:00'
    };

    contractServiceSpy.addAmendment.and.returnValue(of(mockAmendment));
    component.submitAmendment();
    expect(contractServiceSpy.addAmendment).toHaveBeenCalledWith(1, jasmine.any(Object));
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Amendment added successfully');
    expect(component.showAmendmentForm).toBeFalse();
  });

  it('should not submit invalid amendment form', () => {
    component.contract = mockContract;
    component.ngOnInit();
    component.submitAmendment();
    expect(contractServiceSpy.addAmendment).not.toHaveBeenCalled();
  });

  it('should handle amendment submit error', () => {
    component.contract = mockContract;
    component.ngOnInit();
    component.amendmentForm.patchValue({
      amendmentDate: '2023-06-01',
      description: 'Test',
      newTerms: 'New'
    });

    contractServiceSpy.addAmendment.and.returnValue(throwError(() => new Error('fail')));
    component.submitAmendment();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to add amendment');
  });

  it('should renew contract after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    component.contract = mockContract;
    const renewedContract = { ...mockContract, status: 'RENEWED' };
    contractServiceSpy.renew.and.returnValue(of(renewedContract));

    component.renewContract();
    expect(contractServiceSpy.renew).toHaveBeenCalledWith(1);
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Contract renewed successfully');
    expect(component.contract?.status).toBe('RENEWED');
  });

  it('should not renew contract if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.contract = mockContract;
    component.renewContract();
    expect(contractServiceSpy.renew).not.toHaveBeenCalled();
  });

  it('should not renew if no contract loaded', () => {
    component.contract = null;
    component.renewContract();
    expect(contractServiceSpy.renew).not.toHaveBeenCalled();
  });

  it('should handle renew error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    component.contract = mockContract;
    contractServiceSpy.renew.and.returnValue(throwError(() => new Error('fail')));
    component.renewContract();
    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to renew contract');
  });

  it('should return correct status color', () => {
    expect(component.getStatusColor('ACTIVE')).toBe('primary');
    expect(component.getStatusColor('EXPIRED')).toBe('warn');
    expect(component.getStatusColor('RENEWED')).toBe('accent');
    expect(component.getStatusColor('TERMINATED')).toBe('warn');
    expect(component.getStatusColor('UNKNOWN')).toBe('');
  });

  it('should format contract type', () => {
    expect(component.formatType('FIXED_TERM')).toBe('FIXED TERM');
    expect(component.formatType('PERMANENT')).toBe('PERMANENT');
  });

  it('should build timeline items', () => {
    component.contract = mockContract;
    component.amendments = mockAmendments;
    const items = component.getTimelineItems();
    expect(items.length).toBe(3);
    expect(items[0].type).toBe('start');
    expect(items[items.length - 1].type).toBe('end');
  });

  it('should return empty timeline if no contract', () => {
    component.contract = null;
    expect(component.getTimelineItems()).toEqual([]);
  });

  it('should initialize amendment form', () => {
    component.ngOnInit();
    expect(component.amendmentForm).toBeDefined();
    expect(component.amendmentForm.get('amendmentDate')).toBeTruthy();
    expect(component.amendmentForm.get('description')).toBeTruthy();
    expect(component.amendmentForm.get('oldTerms')).toBeTruthy();
    expect(component.amendmentForm.get('newTerms')).toBeTruthy();
  });

  it('should redirect if no id in route', () => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      imports: [ContractDetailComponent, NoopAnimationsModule],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ContractService, useValue: contractServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: { params: of({}), snapshot: { paramMap: { get: () => null } } } }
      ]
    });
    const fix = TestBed.createComponent(ContractDetailComponent);
    fix.componentInstance.ngOnInit();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/contracts']);
  });
});
