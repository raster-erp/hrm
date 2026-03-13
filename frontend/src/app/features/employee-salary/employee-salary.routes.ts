import { Routes } from '@angular/router';
import { EmployeeSalaryListComponent } from './employee-salary-list/employee-salary-list.component';
import { EmployeeSalaryFormComponent } from './employee-salary-form/employee-salary-form.component';

export const EMPLOYEE_SALARY_ROUTES: Routes = [
  { path: '', component: EmployeeSalaryListComponent },
  { path: 'new', component: EmployeeSalaryFormComponent }
];
