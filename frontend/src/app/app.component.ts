import { Component, HostListener, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';

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
    MatSidenavModule, MatToolbarModule, MatListModule,
    MatIconModule, MatButtonModule, MatDividerModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  title = 'HRM System';
  sidenavMode: 'side' | 'over' = 'side';
  sidenavOpened = true;

  private readonly MOBILE_BREAKPOINT = 960;

  navGroups: NavGroup[] = [
    {
      label: 'Employee Management',
      items: [
        { path: '/employees', label: 'Employees', icon: 'people' },
        { path: '/departments', label: 'Departments', icon: 'business' },
        { path: '/designations', label: 'Designations', icon: 'badge' },
      ]
    },
    {
      label: 'Contracts & Credentials',
      items: [
        { path: '/contracts', label: 'Contracts', icon: 'description' },
        { path: '/credentials', label: 'Credentials', icon: 'verified' },
      ]
    },
    {
      label: 'ID Cards & Uniforms',
      items: [
        { path: '/id-cards', label: 'ID Cards', icon: 'credit_card' },
        { path: '/uniforms', label: 'Uniforms', icon: 'checkroom' },
      ]
    },
    {
      label: 'Transfers & Promotions',
      items: [
        { path: '/transfers', label: 'Transfers', icon: 'swap_horiz' },
        { path: '/promotions', label: 'Promotions', icon: 'trending_up' },
      ]
    },
    {
      label: 'Exit Management',
      items: [
        { path: '/separations', label: 'Separations', icon: 'exit_to_app' },
      ]
    }
  ];

  ngOnInit(): void {
    this.updateSidenavForScreenSize();
  }

  @HostListener('window:resize')
  onResize(): void {
    this.updateSidenavForScreenSize();
  }

  onNavClick(): void {
    if (this.sidenavMode === 'over') {
      this.sidenavOpened = false;
    }
  }

  private updateSidenavForScreenSize(): void {
    const isMobile = window.innerWidth < this.MOBILE_BREAKPOINT;
    this.sidenavMode = isMobile ? 'over' : 'side';
    this.sidenavOpened = !isMobile;
  }
}
