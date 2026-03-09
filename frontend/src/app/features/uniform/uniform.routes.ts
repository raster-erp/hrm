import { Routes } from '@angular/router';
import { UniformListComponent } from './uniform-list/uniform-list.component';
import { UniformAllocationListComponent } from './uniform-allocation-list/uniform-allocation-list.component';

export const UNIFORM_ROUTES: Routes = [
  { path: '', component: UniformListComponent },
  { path: 'allocations', component: UniformAllocationListComponent }
];
