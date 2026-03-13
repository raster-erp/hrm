import { Routes } from '@angular/router';
import { SalaryComponentListComponent } from './salary-component-list/salary-component-list.component';
import { SalaryComponentFormComponent } from './salary-component-form/salary-component-form.component';

export const SALARY_COMPONENT_ROUTES: Routes = [
  { path: '', component: SalaryComponentListComponent },
  { path: 'new', component: SalaryComponentFormComponent },
  { path: ':id/edit', component: SalaryComponentFormComponent }
];
