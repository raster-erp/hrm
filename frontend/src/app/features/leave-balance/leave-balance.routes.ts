import { Routes } from '@angular/router';
import { LeaveBalanceDashboardComponent } from './leave-balance-dashboard/leave-balance-dashboard.component';
import { LeaveTransactionHistoryComponent } from './leave-transaction-history/leave-transaction-history.component';
import { YearEndProcessingComponent } from './year-end-processing/year-end-processing.component';

export const LEAVE_BALANCE_ROUTES: Routes = [
  { path: '', component: LeaveBalanceDashboardComponent },
  { path: 'transactions', component: LeaveTransactionHistoryComponent },
  { path: 'year-end', component: YearEndProcessingComponent }
];
