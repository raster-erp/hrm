import { Routes } from '@angular/router';
import { TaxSlabListComponent } from './tax-slab-list/tax-slab-list.component';
import { TaxSlabFormComponent } from './tax-slab-form/tax-slab-form.component';

export const TAX_SLAB_ROUTES: Routes = [
  { path: '', component: TaxSlabListComponent },
  { path: 'new', component: TaxSlabFormComponent },
  { path: ':id/edit', component: TaxSlabFormComponent }
];
