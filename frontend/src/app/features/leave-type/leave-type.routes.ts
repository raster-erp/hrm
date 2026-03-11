import { Routes } from '@angular/router';
import { LeaveTypeListComponent } from './leave-type-list/leave-type-list.component';
import { LeaveTypeFormComponent } from './leave-type-form/leave-type-form.component';

export const LEAVE_TYPE_ROUTES: Routes = [
  { path: '', component: LeaveTypeListComponent },
  { path: 'new', component: LeaveTypeFormComponent },
  { path: ':id/edit', component: LeaveTypeFormComponent }
];
