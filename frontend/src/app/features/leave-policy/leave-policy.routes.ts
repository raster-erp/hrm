import { Routes } from '@angular/router';
import { LeavePolicyListComponent } from './leave-policy-list/leave-policy-list.component';
import { LeavePolicyFormComponent } from './leave-policy-form/leave-policy-form.component';

export const LEAVE_POLICY_ROUTES: Routes = [
  { path: '', component: LeavePolicyListComponent },
  { path: 'new', component: LeavePolicyFormComponent },
  { path: ':id/edit', component: LeavePolicyFormComponent }
];
