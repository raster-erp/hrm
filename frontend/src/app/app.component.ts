import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet, RouterLink, RouterLinkActive,
    MatSidenavModule, MatToolbarModule, MatListModule,
    MatIconModule, MatButtonModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'HRM System';

  navItems = [
    { path: '/employees', label: 'Employees', icon: 'people' },
    { path: '/departments', label: 'Departments', icon: 'business' },
    { path: '/designations', label: 'Designations', icon: 'badge' },
    { path: '/contracts', label: 'Contracts', icon: 'description' },
    { path: '/credentials', label: 'Credentials', icon: 'verified' },
    { path: '/id-cards', label: 'ID Cards', icon: 'credit_card' },
    { path: '/uniforms', label: 'Uniforms', icon: 'checkroom' },
    { path: '/transfers', label: 'Transfers', icon: 'swap_horiz' },
    { path: '/promotions', label: 'Promotions', icon: 'trending_up' },
    { path: '/separations', label: 'Separations', icon: 'exit_to_app' }
  ];
}
