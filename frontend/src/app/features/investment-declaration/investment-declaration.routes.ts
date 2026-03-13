import { Routes } from '@angular/router';
import { InvestmentDeclarationListComponent } from './investment-declaration-list/investment-declaration-list.component';
import { InvestmentDeclarationFormComponent } from './investment-declaration-form/investment-declaration-form.component';

export const INVESTMENT_DECLARATION_ROUTES: Routes = [
  { path: '', component: InvestmentDeclarationListComponent },
  { path: 'new', component: InvestmentDeclarationFormComponent },
  { path: ':id/edit', component: InvestmentDeclarationFormComponent }
];
