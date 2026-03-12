import { Routes } from '@angular/router';
import { LeaveApplicationListComponent } from './leave-application-list/leave-application-list.component';
import { LeaveApplicationFormComponent } from './leave-application-form/leave-application-form.component';
import { LeaveApprovalComponent } from './leave-approval/leave-approval.component';

export const LEAVE_APPLICATION_ROUTES: Routes = [
  { path: '', component: LeaveApplicationListComponent },
  { path: 'new', component: LeaveApplicationFormComponent },
  { path: ':id/edit', component: LeaveApplicationFormComponent },
  { path: 'approval', component: LeaveApprovalComponent }
];
