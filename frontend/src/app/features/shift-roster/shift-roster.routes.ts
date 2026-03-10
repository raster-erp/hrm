import { Routes } from '@angular/router';
import { ShiftRosterListComponent } from './shift-roster-list/shift-roster-list.component';
import { ShiftRosterFormComponent } from './shift-roster-form/shift-roster-form.component';
import { BulkAssignWizardComponent } from './bulk-assign-wizard/bulk-assign-wizard.component';

export const SHIFT_ROSTER_ROUTES: Routes = [
  { path: '', component: ShiftRosterListComponent },
  { path: 'new', component: ShiftRosterFormComponent },
  { path: 'bulk-assign', component: BulkAssignWizardComponent },
  { path: ':id/edit', component: ShiftRosterFormComponent }
];
