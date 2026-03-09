import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CredentialDashboardComponent } from './credential-dashboard.component';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('CredentialDashboardComponent', () => {
  let component: CredentialDashboardComponent;
  let fixture: ComponentFixture<CredentialDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CredentialDashboardComponent, NoopAnimationsModule],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();

    fixture = TestBed.createComponent(CredentialDashboardComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
