import { Routes } from '@angular/router';
import { CredentialListComponent } from './credential-list/credential-list.component';
import { CredentialFormComponent } from './credential-form/credential-form.component';
import { CredentialDetailComponent } from './credential-detail/credential-detail.component';
import { CredentialDashboardComponent } from './credential-dashboard/credential-dashboard.component';

export const CREDENTIAL_ROUTES: Routes = [
  { path: '', component: CredentialListComponent },
  { path: 'dashboard', component: CredentialDashboardComponent },
  { path: 'new', component: CredentialFormComponent },
  { path: ':id', component: CredentialDetailComponent },
  { path: ':id/edit', component: CredentialFormComponent }
];
