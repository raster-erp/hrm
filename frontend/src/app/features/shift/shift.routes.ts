import { Routes } from '@angular/router';
import { ShiftListComponent } from './shift-list/shift-list.component';
import { ShiftFormComponent } from './shift-form/shift-form.component';

export const SHIFT_ROUTES: Routes = [
  { path: '', component: ShiftListComponent },
  { path: 'new', component: ShiftFormComponent },
  { path: ':id/edit', component: ShiftFormComponent }
];
