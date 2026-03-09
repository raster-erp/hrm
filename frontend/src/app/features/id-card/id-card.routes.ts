import { Routes } from '@angular/router';
import { IdCardListComponent } from './id-card-list/id-card-list.component';
import { IdCardFormComponent } from './id-card-form/id-card-form.component';
import { IdCardDetailComponent } from './id-card-detail/id-card-detail.component';

export const ID_CARD_ROUTES: Routes = [
  { path: '', component: IdCardListComponent },
  { path: 'new', component: IdCardFormComponent },
  { path: ':id', component: IdCardDetailComponent },
  { path: ':id/edit', component: IdCardFormComponent }
];
