import { Routes } from '@angular/router';
import { SalaryStructureListComponent } from './salary-structure-list/salary-structure-list.component';
import { SalaryStructureFormComponent } from './salary-structure-form/salary-structure-form.component';

export const SALARY_STRUCTURE_ROUTES: Routes = [
  { path: '', component: SalaryStructureListComponent },
  { path: 'new', component: SalaryStructureFormComponent },
  { path: ':id/edit', component: SalaryStructureFormComponent }
];
