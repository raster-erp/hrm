import { Routes } from '@angular/router';
import { LeavePlanListComponent } from './leave-plan-list/leave-plan-list.component';
import { LeavePlanFormComponent } from './leave-plan-form/leave-plan-form.component';

export const LEAVE_PLANNER_ROUTES: Routes = [
  { path: '', component: LeavePlanListComponent },
  { path: 'new', component: LeavePlanFormComponent }
];
