import { Routes } from '@angular/router';
import { LeaveCalendarViewComponent } from './leave-calendar-view/leave-calendar-view.component';
import { HolidaySetupComponent } from './holiday-setup/holiday-setup.component';
import { TeamAvailabilityComponent } from './team-availability/team-availability.component';

export const LEAVE_CALENDAR_ROUTES: Routes = [
  { path: '', component: LeaveCalendarViewComponent },
  { path: 'holidays', component: HolidaySetupComponent },
  { path: 'team-availability', component: TeamAvailabilityComponent }
];
