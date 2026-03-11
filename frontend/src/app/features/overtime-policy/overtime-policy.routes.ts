import { Routes } from '@angular/router';
import { OvertimePolicyListComponent } from './overtime-policy-list/overtime-policy-list.component';
import { OvertimePolicyFormComponent } from './overtime-policy-form/overtime-policy-form.component';

export const OVERTIME_POLICY_ROUTES: Routes = [
  { path: '', component: OvertimePolicyListComponent },
  { path: 'new', component: OvertimePolicyFormComponent },
  { path: ':id/edit', component: OvertimePolicyFormComponent }
];
