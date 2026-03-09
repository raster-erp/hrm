import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { CredentialFormComponent } from './credential-form.component';
import { CredentialService } from '../../../services/credential.service';
import { EmployeeService } from '../../../services/employee.service';
import { NotificationService } from '../../../services/notification.service';

describe('CredentialFormComponent', () => {
  let component: CredentialFormComponent;
  let fixture: ComponentFixture<CredentialFormComponent>;
  let credentialServiceSpy: jasmine.SpyObj<CredentialService>;
  let employeeServiceSpy: jasmine.SpyObj<EmployeeService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    credentialServiceSpy = jasmine.createSpyObj('CredentialService', ['getById', 'create', 'update']);
    employeeServiceSpy = jasmine.createSpyObj('EmployeeService', ['getAll']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    employeeServiceSpy.getAll.and.returnValue(of({ content: [], totalElements: 0, totalPages: 0, size: 10, number: 0, first: true, last: true, empty: true }));

    await TestBed.configureTestingModule({
      imports: [CredentialFormComponent, HttpClientTestingModule, NoopAnimationsModule],
      providers: [
        { provide: CredentialService, useValue: credentialServiceSpy },
        { provide: EmployeeService, useValue: employeeServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: Router, useValue: routerSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: { get: () => null } }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CredentialFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should be in create mode when no id', () => {
    expect(component.isEditMode).toBeFalse();
  });

  it('should initialize form with required validators', () => {
    expect(component.credentialForm.get('employeeId')?.hasError('required')).toBeTrue();
    expect(component.credentialForm.get('credentialType')?.hasError('required')).toBeTrue();
    expect(component.credentialForm.get('credentialName')?.hasError('required')).toBeTrue();
  });
});
