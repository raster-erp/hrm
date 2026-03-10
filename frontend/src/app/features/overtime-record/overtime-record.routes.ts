import { Routes } from '@angular/router';
import { OvertimeRecordListComponent } from './overtime-record-list/overtime-record-list.component';
import { OvertimeRecordFormComponent } from './overtime-record-form/overtime-record-form.component';

export const OVERTIME_RECORD_ROUTES: Routes = [
  { path: '', component: OvertimeRecordListComponent },
  { path: 'new', component: OvertimeRecordFormComponent },
  { path: ':id/edit', component: OvertimeRecordFormComponent }
];
