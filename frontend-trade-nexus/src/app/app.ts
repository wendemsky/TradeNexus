import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { authStore } from './store/auth.store';
import { PriceStore } from './store/price.store';
import { TokenService } from './core/auth/token.service';
import { environment } from '../environments/environment';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: '<router-outlet/>',
})
export class App implements OnInit, OnDestroy {
  private readonly http         = inject(HttpClient);
  private readonly priceStore   = inject(PriceStore);
  private readonly tokenService = inject(TokenService);
  private refreshInterval?: ReturnType<typeof setInterval>;

  ngOnInit(): void {
    this.applyTheme();
    this.restoreSession();
    this.startRefreshInterval();
  }

  ngOnDestroy(): void {
    clearInterval(this.refreshInterval);
    this.priceStore.disconnect();
  }

  private applyTheme(): void {
    const stored = localStorage.getItem('tn-theme');
    if (stored === 'light') {
      document.documentElement.classList.remove('dark');
    } else {
      document.documentElement.classList.add('dark');
    }
  }

  private restoreSession(): void {
    const profile = authStore.restoreFromSession();
    if (!profile) return;

    if (this.tokenService.isExpired(profile.token)) {
      authStore.logout();
      return;
    }

    authStore.login(profile);
    this.priceStore.connect(environment.wsUrl, profile.token);
  }

  private startRefreshInterval(): void {
    this.refreshInterval = setInterval(() => {
      const token = authStore.profile()?.token;
      if (token && this.tokenService.shouldRefresh(token)) {
        this.http
          .post<{ token: string }>(`${environment.apiUrl}/auth/refresh`, {})
          .subscribe({
            next: ({ token: newToken }) => {
              authStore.updateToken(newToken);
              this.priceStore.reconnect(newToken);
            },
          });
      }
    }, 60_000);
  }
}
