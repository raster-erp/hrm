import { Routes } from '@angular/router';
import { LeaveEncashmentListComponent } from './leave-encashment-list/leave-encashment-list.component';
import { LeaveEncashmentFormComponent } from './leave-encashment-form/leave-encashment-form.component';
import { LeaveEncashmentApprovalComponent } from './leave-encashment-approval/leave-encashment-approval.component';

export const LEAVE_ENCASHMENT_ROUTES: Routes = [
  { path: '', component: LeaveEncashmentListComponent },
  { path: 'new', component: LeaveEncashmentFormComponent },
  { path: 'approval', component: LeaveEncashmentApprovalComponent }
];
