import { Routes } from '@angular/router';
import { RotationPatternListComponent } from './rotation-pattern-list/rotation-pattern-list.component';
import { RotationPatternFormComponent } from './rotation-pattern-form/rotation-pattern-form.component';

export const ROTATION_PATTERN_ROUTES: Routes = [
  { path: '', component: RotationPatternListComponent },
  { path: 'new', component: RotationPatternFormComponent },
  { path: ':id/edit', component: RotationPatternFormComponent }
];
