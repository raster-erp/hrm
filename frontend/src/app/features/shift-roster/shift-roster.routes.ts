import { Routes } from '@angular/router';
import { ShiftRosterListComponent } from './shift-roster-list/shift-roster-list.component';
import { ShiftRosterFormComponent } from './shift-roster-form/shift-roster-form.component';

export const SHIFT_ROSTER_ROUTES: Routes = [
  { path: '', component: ShiftRosterListComponent },
  { path: 'new', component: ShiftRosterFormComponent },
  { path: ':id/edit', component: ShiftRosterFormComponent }
];
