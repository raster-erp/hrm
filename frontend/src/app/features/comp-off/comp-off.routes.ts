import { Routes } from '@angular/router';
import { CompOffListComponent } from './comp-off-list/comp-off-list.component';
import { CompOffFormComponent } from './comp-off-form/comp-off-form.component';
import { CompOffApprovalComponent } from './comp-off-approval/comp-off-approval.component';
import { CompOffBalanceComponent } from './comp-off-balance/comp-off-balance.component';

export const COMP_OFF_ROUTES: Routes = [
  { path: '', component: CompOffListComponent },
  { path: 'new', component: CompOffFormComponent },
  { path: 'approval', component: CompOffApprovalComponent },
  { path: 'balance', component: CompOffBalanceComponent }
];
