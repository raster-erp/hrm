import { Routes } from '@angular/router';
import { WfhRequestListComponent } from './wfh-request-list/wfh-request-list.component';
import { WfhRequestFormComponent } from './wfh-request-form/wfh-request-form.component';
import { WfhCheckinComponent } from './wfh-checkin/wfh-checkin.component';
import { WfhDashboardComponent } from './wfh-dashboard/wfh-dashboard.component';

export const WFH_REQUEST_ROUTES: Routes = [
  { path: '', component: WfhRequestListComponent },
  { path: 'new', component: WfhRequestFormComponent },
  { path: 'checkin', component: WfhCheckinComponent },
  { path: 'dashboard', component: WfhDashboardComponent },
  { path: ':id/edit', component: WfhRequestFormComponent }
];
