import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { CredentialListComponent } from './credential-list.component';
import { CredentialService } from '../../../services/credential.service';
import { NotificationService } from '../../../services/notification.service';

describe('CredentialListComponent', () => {
  let component: CredentialListComponent;
  let fixture: ComponentFixture<CredentialListComponent>;
  let credentialServiceSpy: jasmine.SpyObj<CredentialService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    credentialServiceSpy = jasmine.createSpyObj('CredentialService', ['getAll', 'getByStatus', 'delete']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    credentialServiceSpy.getAll.and.returnValue(of({ content: [], totalElements: 0, totalPages: 0, size: 10, number: 0, first: true, last: true, empty: true }));

    await TestBed.configureTestingModule({
      imports: [CredentialListComponent, HttpClientTestingModule, NoopAnimationsModule],
      providers: [
        { provide: CredentialService, useValue: credentialServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CredentialListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load credentials on init', () => {
    expect(credentialServiceSpy.getAll).toHaveBeenCalled();
  });
});
