import { Routes } from '@angular/router';
import { PayrollListComponent } from './payroll-list/payroll-list.component';
import { PayrollFormComponent } from './payroll-form/payroll-form.component';
import { PayrollDetailComponent } from './payroll-detail/payroll-detail.component';
import { PayrollAdjustmentComponent } from './payroll-adjustment/payroll-adjustment.component';

export const PAYROLL_PROCESSING_ROUTES: Routes = [
  { path: '', component: PayrollListComponent },
  { path: 'new', component: PayrollFormComponent },
  { path: ':id', component: PayrollDetailComponent },
  { path: ':id/adjustments', component: PayrollAdjustmentComponent }
];
