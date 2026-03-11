import { Routes } from '@angular/router';
import { OvertimeRecordListComponent } from './overtime-record-list/overtime-record-list.component';
import { OvertimeRecordFormComponent } from './overtime-record-form/overtime-record-form.component';
import { OvertimeSummaryComponent } from './overtime-summary/overtime-summary.component';

export const OVERTIME_RECORD_ROUTES: Routes = [
  { path: '', component: OvertimeRecordListComponent },
  { path: 'new', component: OvertimeRecordFormComponent },
  { path: 'summary', component: OvertimeSummaryComponent },
  { path: ':id/edit', component: OvertimeRecordFormComponent }
];
