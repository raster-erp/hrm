import { Routes } from '@angular/router';
import { TransferListComponent } from './transfer-list/transfer-list.component';
import { TransferFormComponent } from './transfer-form/transfer-form.component';
import { TransferDetailComponent } from './transfer-detail/transfer-detail.component';

export const TRANSFER_ROUTES: Routes = [
  { path: '', component: TransferListComponent },
  { path: 'new', component: TransferFormComponent },
  { path: ':id', component: TransferDetailComponent }
];
