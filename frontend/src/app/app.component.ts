import { Component, HostListener, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ThemeService } from './services/theme.service';

interface NavGroup {
  label: string;
  items: { path: string; label: string; icon: string }[];
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet, RouterLink, RouterLinkActive,
    MatIconModule, MatButtonModule, MatTooltipModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  title = 'OneHealth HRM';
  sidebarCollapsed = false;
  mobileMenuOpen = false;
  isMobile = false;

  private readonly MOBILE_BP = 960;
  private readonly SIDEBAR_KEY = 'hrm-sidebar';

  navGroups: NavGroup[] = [
    {
      label: 'Organization',
      items: [
        { path: '/employees', label: 'Employees', icon: 'people' },
        { path: '/departments', label: 'Departments', icon: 'apartment' },
        { path: '/designations', label: 'Designations', icon: 'badge' },
      ]
    },
    {
      label: 'Records',
      items: [
        { path: '/contracts', label: 'Contracts', icon: 'description' },
        { path: '/credentials', label: 'Credentials', icon: 'verified' },
        { path: '/id-cards', label: 'ID Cards', icon: 'credit_card' },
        { path: '/uniforms', label: 'Uniforms', icon: 'checkroom' },
      ]
    },
    {
      label: 'Attendance',
      items: [
        { path: '/devices', label: 'Devices', icon: 'devices' },
        { path: '/attendance-punches', label: 'Punch Log', icon: 'schedule' },
        { path: '/shifts', label: 'Shifts', icon: 'access_time' },
        { path: '/rotation-patterns', label: 'Rotation Patterns', icon: 'autorenew' },
        { path: '/shift-rosters', label: 'Rosters', icon: 'calendar_month' },
        { path: '/overtime-policies', label: 'OT Policies', icon: 'policy' },
        { path: '/overtime-records', label: 'OT Records', icon: 'more_time' },
        { path: '/overtime-records/summary', label: 'OT Summary', icon: 'bar_chart' },
        { path: '/attendance-deviations', label: 'Deviations', icon: 'running_with_errors' },
        { path: '/attendance-deviations/summary', label: 'Deviation Summary', icon: 'assessment' },
        { path: '/attendance-regularization', label: 'Regularization', icon: 'assignment_return' },
        { path: '/attendance-regularization/approval', label: 'Approval Inbox', icon: 'inbox' },
        { path: '/wfh-requests', label: 'WFH Requests', icon: 'home_work' },
        { path: '/wfh-requests/checkin', label: 'WFH Check-in', icon: 'login' },
        { path: '/wfh-requests/dashboard', label: 'WFH Dashboard', icon: 'dashboard' },
        { path: '/attendance-reports', label: 'Reports', icon: 'summarize' },
        { path: '/attendance-reports/schedules', label: 'Report Schedules', icon: 'schedule_send' },
      ]
    },
    {
      label: 'Leave',
      items: [
        { path: '/leave-types', label: 'Leave Types', icon: 'event_note' },
        { path: '/leave-policies', label: 'Leave Policies', icon: 'policy' },
        { path: '/leave-policy-assignments', label: 'Policy Assignments', icon: 'assignment_ind' },
        { path: '/leave-applications', label: 'Leave Applications', icon: 'event_busy' },
        { path: '/leave-applications/approval', label: 'Leave Approvals', icon: 'inbox' },
        { path: '/leave-balances', label: 'Leave Balances', icon: 'account_balance_wallet' },
        { path: '/leave-balances/transactions', label: 'Leave Transactions', icon: 'receipt_long' },
        { path: '/leave-balances/year-end', label: 'Year-End Processing', icon: 'event_repeat' },
        { path: '/leave-encashments', label: 'Leave Encashments', icon: 'payments' },
        { path: '/leave-encashments/approval', label: 'Encashment Approvals', icon: 'approval' },
        { path: '/comp-off', label: 'Comp-Off Requests', icon: 'event_available' },
        { path: '/comp-off/approval', label: 'Comp-Off Approvals', icon: 'approval' },
        { path: '/comp-off/balance', label: 'Comp-Off Balance', icon: 'account_balance_wallet' },
        { path: '/leave-planner', label: 'Leave Planner', icon: 'edit_calendar' },
        { path: '/leave-calendar', label: 'Leave Calendar', icon: 'calendar_month' },
        { path: '/leave-calendar/holidays', label: 'Holiday Setup', icon: 'celebration' },
        { path: '/leave-calendar/team-availability', label: 'Team Availability', icon: 'groups' },
        { path: '/leave-analytics', label: 'Analytics Reports', icon: 'analytics' },
      ]
    },
    {
      label: 'Payroll',
      items: [
        { path: '/salary-components', label: 'Salary Components', icon: 'account_balance' },
        { path: '/salary-structures', label: 'Salary Structures', icon: 'account_tree' },
        { path: '/employee-salary', label: 'Salary Assignments', icon: 'payments' },
        { path: '/payroll-processing', label: 'Payroll Processing', icon: 'calculate' },
      ]
    },
    {
      label: 'Actions',
      items: [
        { path: '/transfers', label: 'Transfers', icon: 'swap_horiz' },
        { path: '/promotions', label: 'Promotions', icon: 'trending_up' },
        { path: '/separations', label: 'Separations', icon: 'exit_to_app' },
      ]
    }
  ];

  constructor(public themeService: ThemeService) {
    const saved = localStorage.getItem(this.SIDEBAR_KEY);
    if (saved === 'true') this.sidebarCollapsed = true;
  }

  ngOnInit(): void {
    this.checkScreen();
  }

  @HostListener('window:resize')
  onResize(): void {
    this.checkScreen();
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
    localStorage.setItem(this.SIDEBAR_KEY, String(this.sidebarCollapsed));
  }

  onNavClick(): void {
    if (this.isMobile) {
      this.mobileMenuOpen = false;
    }
  }

  private checkScreen(): void {
    this.isMobile = window.innerWidth < this.MOBILE_BP;
    if (this.isMobile) {
      this.mobileMenuOpen = false;
    }
  }
}
