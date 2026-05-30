import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { LandingComponent } from './pages/auth/landing/landing.component';
import { RegistrationComponent } from './pages/auth/registration/registration.component';
import { DashboardShellComponent } from './pages/dashboard/dashboard-shell/dashboard-shell.component';
import { InstrumentsComponent } from './pages/dashboard/instruments/instruments.component';
import { PortfolioComponent } from './pages/dashboard/portfolio/portfolio.component';
import { TradingHistoryComponent } from './pages/dashboard/trading-history/trading-history.component';
import { PreferencesComponent } from './pages/dashboard/preferences/preferences.component';
import { ActivityReportComponent } from './pages/dashboard/activity-report/activity-report.component';

export const routes: Routes = [
  { path: '', component: LandingComponent },
  { path: 'register', component: RegistrationComponent },
  {
    path: 'dashboard',
    component: DashboardShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '',               redirectTo: 'instruments', pathMatch: 'full' },
      { path: 'instruments',    component: InstrumentsComponent },
      { path: 'portfolio',      component: PortfolioComponent },
      { path: 'trading-history',component: TradingHistoryComponent },
      { path: 'preferences',    component: PreferencesComponent },
      { path: 'activity-report',component: ActivityReportComponent },
    ],
  },
  { path: '**', redirectTo: '' },
];
