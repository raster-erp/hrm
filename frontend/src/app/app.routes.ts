import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'employees', pathMatch: 'full' },
  {
    path: 'employees',
    loadChildren: () => import('./features/employee/employee.routes').then(m => m.EMPLOYEE_ROUTES)
  },
  {
    path: 'departments',
    loadChildren: () => import('./features/department/department.routes').then(m => m.DEPARTMENT_ROUTES)
  },
  {
    path: 'designations',
    loadChildren: () => import('./features/designation/designation.routes').then(m => m.DESIGNATION_ROUTES)
  },
  {
    path: 'contracts',
    loadChildren: () => import('./features/contract/contract.routes').then(m => m.CONTRACT_ROUTES)
  },
  {
    path: 'credentials',
    loadChildren: () => import('./features/credential/credential.routes').then(m => m.CREDENTIAL_ROUTES)
  },
  {
    path: 'id-cards',
    loadChildren: () => import('./features/id-card/id-card.routes').then(m => m.ID_CARD_ROUTES)
  },
  {
    path: 'uniforms',
    loadChildren: () => import('./features/uniform/uniform.routes').then(m => m.UNIFORM_ROUTES)
  },
  {
    path: 'transfers',
    loadChildren: () => import('./features/transfer/transfer.routes').then(m => m.TRANSFER_ROUTES)
  },
  {
    path: 'promotions',
    loadChildren: () => import('./features/promotion/promotion.routes').then(m => m.PROMOTION_ROUTES)
  },
  {
    path: 'separations',
    loadChildren: () => import('./features/separation/separation.routes').then(m => m.SEPARATION_ROUTES)
  }
];
