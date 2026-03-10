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
