import { Routes } from '@angular/router';
import { ContractListComponent } from './contract-list/contract-list.component';
import { ContractFormComponent } from './contract-form/contract-form.component';
import { ContractDetailComponent } from './contract-detail/contract-detail.component';

export const CONTRACT_ROUTES: Routes = [
  { path: '', component: ContractListComponent },
  { path: 'new', component: ContractFormComponent },
  { path: ':id', component: ContractDetailComponent },
  { path: ':id/edit', component: ContractFormComponent }
];
