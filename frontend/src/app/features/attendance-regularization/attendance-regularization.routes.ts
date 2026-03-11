import { Routes } from '@angular/router';
import { RegularizationListComponent } from './regularization-list/regularization-list.component';
import { RegularizationFormComponent } from './regularization-form/regularization-form.component';
import { RegularizationApprovalComponent } from './regularization-approval/regularization-approval.component';

export const ATTENDANCE_REGULARIZATION_ROUTES: Routes = [
  { path: '', component: RegularizationListComponent },
  { path: 'new', component: RegularizationFormComponent },
  { path: ':id/edit', component: RegularizationFormComponent },
  { path: 'approval', component: RegularizationApprovalComponent }
];
