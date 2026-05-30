import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CurrencyPipe } from '@angular/common';
import { PortfolioStore } from '../../../store/portfolio.store';
import { PortfolioService } from '../../../core/services/portfolio.service';
import { authStore } from '../../../store/auth.store';
import { PriceStore } from '../../../store/price.store';

interface NavItem {
  label: string;
  path:  string;
  icon:  string;
}

@Component({
  selector: 'tn-dashboard-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CurrencyPipe],
  templateUrl: './dashboard-shell.component.html',
})
export class DashboardShellComponent implements OnInit {
  private readonly router         = inject(Router);
  private readonly portfolioStore = inject(PortfolioStore);
  private readonly portfolioSvc   = inject(PortfolioService);
  private readonly priceStore     = inject(PriceStore);

  readonly balance     = this.portfolioStore.balance;
  readonly clientName  = authStore.profile;
  readonly isMobileNavOpen = signal(false);

  readonly navItems: NavItem[] = [
    { label: 'Instruments',     path: '/dashboard/instruments',    icon: 'chart-bar' },
    { label: 'Portfolio',       path: '/dashboard/portfolio',       icon: 'briefcase' },
    { label: 'Trading History', path: '/dashboard/trading-history', icon: 'clock' },
    { label: 'Preferences',     path: '/dashboard/preferences',     icon: 'adjustments' },
    { label: 'Activity Report', path: '/dashboard/activity-report', icon: 'document-report' },
  ];

  readonly theme = signal<'dark' | 'light'>(
    document.documentElement.classList.contains('dark') ? 'dark' : 'light'
  );

  ngOnInit(): void {
    const clientId = authStore.clientId();
    if (!clientId) return;

    this.portfolioStore.registerLoadFn(id => {
      this.portfolioSvc.getPortfolio(id).subscribe({
        next: portfolio => this.portfolioStore.setPortfolio(portfolio),
      });
    });

    this.portfolioStore.load(clientId);
  }

  logout(): void {
    authStore.logout();
    this.priceStore.disconnect();
    this.portfolioStore.clear();
    this.router.navigate(['/']);
  }

  toggleTheme(): void {
    const html = document.documentElement;
    if (html.classList.contains('dark')) {
      html.classList.remove('dark');
      localStorage.setItem('tn-theme', 'light');
      this.theme.set('light');
    } else {
      html.classList.add('dark');
      localStorage.setItem('tn-theme', 'dark');
      this.theme.set('dark');
    }
  }

  iconPath(icon: string): string {
    const paths: Record<string, string> = {
      'chart-bar':       'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z',
      'briefcase':       'M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z',
      'clock':           'M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z',
      'adjustments':     'M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4',
      'document-report': 'M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414A1 1 0 0119 9.414V19a2 2 0 01-2 2z',
    };
    return paths[icon] ?? '';
  }
}
