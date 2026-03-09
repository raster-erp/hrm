import { Routes } from '@angular/router';

export const PROMOTION_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./promotion-list/promotion-list.component').then(m => m.PromotionListComponent)
  },
  {
    path: 'new',
    loadComponent: () => import('./promotion-form/promotion-form.component').then(m => m.PromotionFormComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./promotion-detail/promotion-detail.component').then(m => m.PromotionDetailComponent)
  }
];
