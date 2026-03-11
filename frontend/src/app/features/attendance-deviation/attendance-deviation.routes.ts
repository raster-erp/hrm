import { Routes } from '@angular/router';
import { DeviationListComponent } from './deviation-list/deviation-list.component';
import { DeviationSummaryComponent } from './deviation-summary/deviation-summary.component';

export const ATTENDANCE_DEVIATION_ROUTES: Routes = [
  { path: '', component: DeviationListComponent },
  { path: 'summary', component: DeviationSummaryComponent }
];
