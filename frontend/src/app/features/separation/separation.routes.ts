import { Routes } from '@angular/router';

export const SEPARATION_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./separation-list/separation-list.component').then(m => m.SeparationListComponent)
  },
  {
    path: 'new',
    loadComponent: () => import('./separation-form/separation-form.component').then(m => m.SeparationFormComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./separation-detail/separation-detail.component').then(m => m.SeparationDetailComponent)
  }
];
