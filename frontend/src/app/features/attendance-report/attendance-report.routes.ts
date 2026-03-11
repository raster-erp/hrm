import { Routes } from '@angular/router';
import { ReportViewerComponent } from './report-viewer/report-viewer.component';
import { ReportScheduleListComponent } from './report-schedule-list/report-schedule-list.component';

export const ATTENDANCE_REPORT_ROUTES: Routes = [
  { path: '', component: ReportViewerComponent },
  { path: 'schedules', component: ReportScheduleListComponent }
];
