import { Routes } from '@angular/router';
import { DeviceListComponent } from './device-list/device-list.component';
import { DeviceFormComponent } from './device-form/device-form.component';

export const DEVICE_ROUTES: Routes = [
  { path: '', component: DeviceListComponent },
  { path: 'new', component: DeviceFormComponent },
  { path: ':id/edit', component: DeviceFormComponent }
];
