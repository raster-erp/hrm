import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { InvestmentDeclarationListComponent } from './investment-declaration-list.component';
import { InvestmentDeclarationService } from '../../../services/investment-declaration.service';
import { NotificationService } from '../../../services/notification.service';
import { Page } from '../../../models/page.model';
import { InvestmentDeclarationResponse } from '../../../models/investment-declaration.model';

describe('InvestmentDeclarationListComponent', () => {
  let component: InvestmentDeclarationListComponent;
  let fixture: ComponentFixture<InvestmentDeclarationListComponent>;
  let serviceSpy: jasmine.SpyObj<InvestmentDeclarationService>;
  let notificationSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockPage: Page<InvestmentDeclarationResponse> = {
    content: [
      {
        id: 1, employeeId: 101, employeeName: 'John Doe',
        financialYear: '2025-26', regime: 'OLD',
        totalDeclaredAmount: 200000, totalVerifiedAmount: 0,
        status: 'DRAFT', remarks: null,
        submittedAt: null, verifiedAt: null, verifiedBy: null,
        items: [],
        createdAt: '2026-01-01T00:00:00', updatedAt: '2026-01-01T00:00:00'
      }
    ],
    totalElements: 1, totalPages: 1, size: 10, number: 0,
    first: true, last: true, empty: false
  };

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('InvestmentDeclarationService',
      ['getAll', 'submit', 'verify', 'reject', 'delete']);
    notificationSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    serviceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [
        InvestmentDeclarationListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: InvestmentDeclarationService, useValue: serviceSpy },
        { provide: NotificationService, useValue: notificationSpy },
        { provide: Router, useValue: routerSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({}),
            snapshot: { paramMap: { get: () => null } }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(InvestmentDeclarationListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have displayed columns defined', () => {
    expect(component.displayedColumns.length).toBeGreaterThan(0);
  });

  it('should have default page size of 10', () => {
    expect(component.pageSize).toBe(10);
  });

  it('should load declarations on init', () => {
    component.ngOnInit();
    expect(serviceSpy.getAll).toHaveBeenCalledWith(0, 10);
    expect(component.dataSource.data.length).toBe(1);
  });

  it('should handle load error', () => {
    serviceSpy.getAll.and.returnValue(throwError(() => new Error('fail')));
    component.loadDeclarations();
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to load investment declarations');
  });

  it('should update pagination on page change', () => {
    component.onPageChange({ pageIndex: 2, pageSize: 25, length: 100 });
    expect(component.pageIndex).toBe(2);
    expect(component.pageSize).toBe(25);
  });

  it('should navigate to edit page', () => {
    component.editDeclaration(mockPage.content[0]);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/investment-declarations', 1, 'edit']);
  });

  it('should submit declaration', () => {
    serviceSpy.submit.and.returnValue(of(mockPage.content[0]));
    component.submitDeclaration(mockPage.content[0]);
    expect(serviceSpy.submit).toHaveBeenCalledWith(1);
    expect(notificationSpy.success).toHaveBeenCalledWith('Declaration submitted successfully');
  });

  it('should handle submit error', () => {
    serviceSpy.submit.and.returnValue(throwError(() => new Error('fail')));
    component.submitDeclaration(mockPage.content[0]);
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to submit declaration');
  });

  it('should verify declaration', () => {
    serviceSpy.verify.and.returnValue(of(mockPage.content[0]));
    component.verifyDeclaration(mockPage.content[0]);
    expect(serviceSpy.verify).toHaveBeenCalledWith(1, 1);
    expect(notificationSpy.success).toHaveBeenCalledWith('Declaration verified successfully');
  });

  it('should handle verify error', () => {
    serviceSpy.verify.and.returnValue(throwError(() => new Error('fail')));
    component.verifyDeclaration(mockPage.content[0]);
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to verify declaration');
  });

  it('should reject declaration with remarks', () => {
    spyOn(window, 'prompt').and.returnValue('Incomplete documents');
    serviceSpy.reject.and.returnValue(of(mockPage.content[0]));
    component.rejectDeclaration(mockPage.content[0]);
    expect(serviceSpy.reject).toHaveBeenCalledWith(1, 'Incomplete documents');
    expect(notificationSpy.success).toHaveBeenCalledWith('Declaration rejected');
  });

  it('should not reject declaration if no remarks provided', () => {
    spyOn(window, 'prompt').and.returnValue(null);
    component.rejectDeclaration(mockPage.content[0]);
    expect(serviceSpy.reject).not.toHaveBeenCalled();
  });

  it('should handle reject error', () => {
    spyOn(window, 'prompt').and.returnValue('Bad docs');
    serviceSpy.reject.and.returnValue(throwError(() => new Error('fail')));
    component.rejectDeclaration(mockPage.content[0]);
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to reject declaration');
  });

  it('should delete declaration after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    serviceSpy.delete.and.returnValue(of(void 0));
    component.deleteDeclaration(mockPage.content[0]);
    expect(serviceSpy.delete).toHaveBeenCalledWith(1);
    expect(notificationSpy.success).toHaveBeenCalledWith('Declaration deleted successfully');
  });

  it('should not delete declaration if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteDeclaration(mockPage.content[0]);
    expect(serviceSpy.delete).not.toHaveBeenCalled();
  });

  it('should handle delete error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    serviceSpy.delete.and.returnValue(throwError(() => new Error('fail')));
    component.deleteDeclaration(mockPage.content[0]);
    expect(notificationSpy.error).toHaveBeenCalledWith('Failed to delete declaration');
  });
});
