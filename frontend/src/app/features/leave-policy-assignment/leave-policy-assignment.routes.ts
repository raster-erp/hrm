import { Routes } from '@angular/router';
import { LeavePolicyAssignmentListComponent } from './leave-policy-assignment-list/leave-policy-assignment-list.component';
import { LeavePolicyAssignmentFormComponent } from './leave-policy-assignment-form/leave-policy-assignment-form.component';

export const LEAVE_POLICY_ASSIGNMENT_ROUTES: Routes = [
  { path: '', component: LeavePolicyAssignmentListComponent },
  { path: 'new', component: LeavePolicyAssignmentFormComponent },
  { path: ':id/edit', component: LeavePolicyAssignmentFormComponent }
];
