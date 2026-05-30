# Frontend Service — Implementation Spec

**Directory:** `frontend-trade-nexus/`
**Port:** 4200 (dev server)
**Stack:** Angular 18, TypeScript 5, Tailwind CSS 3.4, SPARTAN UI, AG Grid Community 32
**Phase:** 4 (last — contracts must be stable before UI is built)
**Branch:** `feature/frontend/angular18`

---

## Responsibility

Complete rewrite from scratch. The old Angular 15 codebase is deleted entirely. The new project:
- Standalone components throughout — no NgModules
- Angular Signals for reactive state — no BehaviorSubject, no NgRx
- Tailwind CSS as the styling foundation — no Angular Material
- SPARTAN UI (spartan/ng) as the component system — Angular-native shadcn/ui equivalent, built on Angular CDK
- WebSocket consumer for live prices via `PriceStore`
- Institutional dark trading terminal aesthetic — trust, readability, long-session usability
- Default dark mode; light mode via Tailwind class toggle
- Desktop-first layout with genuine mobile responsiveness (375px+)
- JWT auth with automatic token refresh

---

## Design Philosophy

> Institutional, data-driven, suitable for managing real financial decisions.
> Trust and readability over aesthetics.

- **Brand colors are separate from financial indicator colors.** Green/red is reserved for P&L and price direction — never for brand UI elements.
- **Typography:** Inter (UI text) + JetBrains Mono / Fira Code (price cells and numeric data only)
- **Reduced-fatigue darks:** soft navy-blacks (`#0f1117`), not pure black
- **Data-dense layouts:** compact spacing, information-forward, no decorative whitespace
- **Accessibility:** WCAG AA contrast minimum on all text/background combinations

---

## Color System

CSS custom properties defined on `:root` (light) and `.dark`. Mapped into Tailwind via `extend.colors`.

### Dark mode (default)

| Token | Hex | Tailwind class | Usage |
|-------|-----|----------------|-------|
| `--color-background` | `#0f1117` | `bg-background` | Page background |
| `--color-surface` | `#1a1d23` | `bg-surface` | Cards, panels, dialogs |
| `--color-surface-raised` | `#22262e` | `bg-surface-raised` | Hover states, nested surfaces |
| `--color-border` | `#2a2d35` | `border-border` | Dividers, input borders |
| `--color-text-primary` | `#e2e8f0` | `text-text-primary` | Body text, labels |
| `--color-text-secondary` | `#94a3b8` | `text-text-secondary` | Muted labels, metadata |
| `--color-brand` | `#3b82f6` | `text-brand`, `bg-brand` | Buttons, links, active nav |
| `--color-brand-hover` | `#2563eb` | `hover:bg-brand-hover` | Button hover state |
| `--color-positive` | `#22c55e` | `text-positive` | Positive P&L, price up |
| `--color-negative` | `#ef4444` | `text-negative` | Negative P&L, price down |
| `--color-caution` | `#f59e0b` | `text-caution` | LIMIT order badge, pending states |

### Light mode

| Token | Hex | Usage |
|-------|-----|-------|
| `--color-background` | `#f8fafc` | Page background |
| `--color-surface` | `#ffffff` | Cards, panels |
| `--color-surface-raised` | `#f1f5f9` | Hover states |
| `--color-border` | `#e2e8f0` | Dividers |
| `--color-text-primary` | `#0f172a` | Body text |
| `--color-text-secondary` | `#64748b` | Muted |
| `--color-brand` | `#2563eb` | Buttons, links |
| `--color-brand-hover` | `#1d4ed8` | Button hover |
| `--color-positive` | `#16a34a` | Positive P&L |
| `--color-negative` | `#dc2626` | Negative P&L |
| `--color-caution` | `#d97706` | LIMIT badge |

The `.dark` class is applied to `<html>` on bootstrap (default). An inline script in `index.html` reads `localStorage('tn-theme')` before Angular loads to prevent flash of wrong theme.

---

## Responsiveness Strategy

Desktop-first: primary layout targets 1280px+. Mobile: genuine usability at 375px+.

### Breakpoints (Tailwind defaults)

| Prefix | Min-width | Role |
|--------|-----------|------|
| base | — | Mobile |
| `md:` | 768px | Tablet |
| `lg:` | 1024px | Desktop (primary) |
| `xl:` | 1280px | Wide desktop |

### Per-area responsive rules

**Dashboard shell**
- `lg:` — fixed left sidebar (240px), content fills remaining width
- base/`md:` — sidebar hidden; bottom tab bar with 5 icon tabs (Instruments, Portfolio, History, Preferences, Activity)
- Hamburger on `md:` opens SPARTAN Sheet drawer for full sidebar

**AG Grid (all instances)**
- `lg:` — all columns visible
- `md:` — hide secondary columns (e.g. Trade ID; keep Direction, Value, Date)
- base — 3–4 essential columns; horizontal scroll enabled
- Column visibility controlled via `BreakpointObserver` from `@angular/cdk/layout`

**Dialogs (TradingFormDialog, RoboAdvisorDialog)**
- `lg:` — `max-w-lg` centered, standard dialog
- base/`md:` — full-screen (`w-screen h-screen rounded-none`)

**Charts**
- Chart.js: `responsive: true`, `maintainAspectRatio: false`, container constrained by CSS height
- Lightweight Charts: `autoSize: true`

**Registration stepper**
- `lg:` — horizontal stepper
- base/`md:` — vertical stepper

**Forms**
- Full-width inputs on all breakpoints
- Two-column grid on `lg:`, single-column on base

---

## Project Bootstrap

```bash
# From repo root — delete old frontend first
rm -rf frontend-trade-nexus

ng new frontend-trade-nexus \
  --routing=true \
  --style=scss \
  --standalone=true \
  --skip-git=true

cd frontend-trade-nexus

# Tailwind CSS
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init

# SPARTAN UI (Angular-native shadcn/ui)
ng add @spartan-ng/cli

# Add SPARTAN primitives needed for this app
npx nx g @spartan-ng/cli:ui button
npx nx g @spartan-ng/cli:ui input
npx nx g @spartan-ng/cli:ui label
npx nx g @spartan-ng/cli:ui card
npx nx g @spartan-ng/cli:ui dialog
npx nx g @spartan-ng/cli:ui select
npx nx g @spartan-ng/cli:ui tabs
npx nx g @spartan-ng/cli:ui badge
npx nx g @spartan-ng/cli:ui sheet
npx nx g @spartan-ng/cli:ui toggle
npx nx g @spartan-ng/cli:ui switch
npx nx g @spartan-ng/cli:ui separator
npx nx g @spartan-ng/cli:ui avatar
npx nx g @spartan-ng/cli:ui alert
npx nx g @spartan-ng/cli:ui stepper
npx nx g @spartan-ng/cli:ui slider
npx nx g @spartan-ng/cli:ui scroll-area
npx nx g @spartan-ng/cli:ui tooltip

# Data grid + charts + utilities
npm install ag-grid-angular@32 ag-grid-community@32
npm install chart.js@4
npm install lightweight-charts@4
npm install jwt-decode@4
npm install xlsx
```

**Do NOT install:** `ag-grid-enterprise`, `@angular/material`, `crypto-js`, `@auth0/angular-jwt`

Register AG Grid once in `main.ts` before `bootstrapApplication`:
```typescript
import { ModuleRegistry, AllCommunityModule } from 'ag-grid-community';
ModuleRegistry.registerModules([AllCommunityModule]);
```

Add to `angular.json` styles array:
```json
"node_modules/ag-grid-community/styles/ag-grid.css",
"node_modules/ag-grid-community/styles/ag-theme-alpine.css"
```

---

## Project Structure

```
frontend-trade-nexus/src/
  app/
    core/
      auth/
        auth.guard.ts             # canActivateFn — checks token expiry
        auth.interceptor.ts       # HttpInterceptorFn — adds Authorization header, handles 401
        token.service.ts          # isExpired(), shouldRefresh() via jwt-decode
      services/
        client.service.ts         # login, register, refresh, verify-email, ping
        portfolio.service.ts      # GET /portfolio/client/:id
        trade.service.ts          # POST /trade/execute-trade, GET /trade/trade-history/:id
        robo-advisor.service.ts   # POST /trade/suggest-buy|sell
        preferences.service.ts    # GET/POST/PUT /client-preferences
        activity.service.ts       # GET /activity-report/{holdings|trades|pl}/:id
        mds.service.ts            # GET {mdsHttpUrl}/prices, /prices/:id/history
    store/
      auth.store.ts               # Module-level signal: ClientProfile | null
      price.store.ts              # Injectable: Map<instrumentId, Price> signal + WS consumer
      portfolio.store.ts          # Injectable: ClientPortfolio signal + computed holdingWithPL
    pages/
      auth/
        landing/                  # Login form + hero section
        registration/             # 3-step SPARTAN Stepper
      dashboard/
        dashboard-shell/          # Sidebar layout (desktop) + bottom tab bar (mobile)
        instruments/              # AG Grid live prices + Buy/Sell
        portfolio/                # Holdings grid + Chart.js doughnut
        trading-history/          # AG Grid trade history
        preferences/              # Robo advisor prefs form
        activity-report/          # 3 SPARTAN Tabs + SheetJS export
    shared/
      components/
        trading-form-dialog/      # MARKET/LIMIT dialog — live price + fee estimate
        robo-advisor-dialog/      # Buy/Sell recommendation grids
        price-flash-cell/         # AG Grid ICellRendererAngularComp — green/red flash
      models/
        client.models.ts          # Client, ClientProfile, ClientPortfolio, Holding, HoldingWithPL, ClientPreferences
        price.models.ts           # Instrument, Price, MarketStatus
        trade.models.ts           # Order, Trade, TradeHistory, TradePL
      validators/
        password.validator.ts     # uppercase + lowercase + digit + min 8
        id.validator.ts           # Aadhar 12d / PAN 5U4d1U / SSN 9d — dynamic
    app.routes.ts
    app.config.ts
  lib/
    ui-*/                         # SPARTAN generated component source (treat as local code)
  environments/
    environment.ts
    environment.prod.ts
```

---

## Tailwind Configuration (`tailwind.config.js`)

```js
module.exports = {
  darkMode: 'class',
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        background:        'var(--color-background)',
        surface:           'var(--color-surface)',
        'surface-raised':  'var(--color-surface-raised)',
        border:            'var(--color-border)',
        'text-primary':    'var(--color-text-primary)',
        'text-secondary':  'var(--color-text-secondary)',
        brand:             'var(--color-brand)',
        'brand-hover':     'var(--color-brand-hover)',
        positive:          'var(--color-positive)',
        negative:          'var(--color-negative)',
        caution:           'var(--color-caution)',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
      },
    },
  },
};
```

---

## `styles.scss`

```scss
@tailwind base;
@tailwind components;
@tailwind utilities;

:root {
  --color-background:    #f8fafc;
  --color-surface:       #ffffff;
  --color-surface-raised:#f1f5f9;
  --color-border:        #e2e8f0;
  --color-text-primary:  #0f172a;
  --color-text-secondary:#64748b;
  --color-brand:         #2563eb;
  --color-brand-hover:   #1d4ed8;
  --color-positive:      #16a34a;
  --color-negative:      #dc2626;
  --color-caution:       #d97706;
}

.dark {
  --color-background:    #0f1117;
  --color-surface:       #1a1d23;
  --color-surface-raised:#22262e;
  --color-border:        #2a2d35;
  --color-text-primary:  #e2e8f0;
  --color-text-secondary:#94a3b8;
  --color-brand:         #3b82f6;
  --color-brand-hover:   #2563eb;
  --color-positive:      #22c55e;
  --color-negative:      #ef4444;
  --color-caution:       #f59e0b;
}

body { @apply bg-background text-text-primary font-sans; }

/* Price and numeric cells — monospace for alignment */
.price-cell { @apply font-mono tabular-nums; }

/* AG Grid — CSS variable overrides to match color system */
.ag-theme-alpine,
.ag-theme-alpine-dark {
  --ag-background-color:         var(--color-surface);
  --ag-header-background-color:  var(--color-background);
  --ag-odd-row-background-color: var(--color-surface);
  --ag-row-hover-color:          var(--color-surface-raised);
  --ag-border-color:             var(--color-border);
  --ag-foreground-color:         var(--color-text-primary);
  --ag-header-foreground-color:  var(--color-text-secondary);
  --ag-font-family:              Inter, system-ui, sans-serif;
  --ag-font-size:                13px;
}

/* Financial indicator utility classes */
.text-positive { color: var(--color-positive); }
.text-negative { color: var(--color-negative); }
.text-caution  { color: var(--color-caution); }

/* Price flash animations */
@keyframes flash-positive {
  0%, 100% { background-color: transparent; }
  40%      { background-color: color-mix(in srgb, var(--color-positive) 20%, transparent); }
}
@keyframes flash-negative {
  0%, 100% { background-color: transparent; }
  40%      { background-color: color-mix(in srgb, var(--color-negative) 20%, transparent); }
}
.price-flash-up   { animation: flash-positive 0.8s ease; }
.price-flash-down { animation: flash-negative 0.8s ease; }
```

---

## Signal Stores

### Auth Store (`store/auth.store.ts`) — module-level, not injectable

Module-level (not `@Injectable`) so it can be accessed from functional interceptors and guards
outside Angular's DI injection context.

```typescript
import { signal, computed } from '@angular/core';
import { ClientProfile } from '../shared/models/client.models';

const SESSION_KEY = 'tn-session';

const _profile = signal<ClientProfile | null>(null);

export const authStore = {
  profile:  _profile.asReadonly(),
  clientId: computed(() => _profile()?.client.clientId ?? null),
  isAdmin:  computed(() => _profile()?.client.isAdmin ?? false),

  login(profile: ClientProfile): void {
    _profile.set(profile);
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(profile));
  },

  logout(): void {
    _profile.set(null);
    sessionStorage.removeItem(SESSION_KEY);
  },

  updateToken(token: string): void {
    const current = _profile();
    if (!current) return;
    const updated = { ...current, token };
    _profile.set(updated);
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(updated));
  },

  restoreFromSession(): ClientProfile | null {
    const raw = sessionStorage.getItem(SESSION_KEY);
    if (!raw) return null;
    try { return JSON.parse(raw) as ClientProfile; } catch { return null; }
  },
};
```

### Price Store (`store/price.store.ts`) — injectable

```typescript
@Injectable({ providedIn: 'root' })
export class PriceStore {
  private readonly _prices = signal<Map<string, Price>>(new Map());
  private ws: WebSocket | null = null;
  private reconnectDelay = 1000;
  private reconnectTimer?: ReturnType<typeof setTimeout>;
  private currentWsUrl = '';

  readonly prices    = this._prices.asReadonly();
  readonly priceList = computed(() => Array.from(this._prices().values()));

  connect(wsUrl: string, token: string): void {
    this.currentWsUrl = wsUrl;
    this.ws = new WebSocket(wsUrl);

    this.ws.onopen = () => {
      this.ws!.send(JSON.stringify({ type: 'AUTH', token }));
      this.reconnectDelay = 1000;  // reset on successful open
    };

    this.ws.onmessage = (event) => {
      const frame = JSON.parse(event.data as string);
      if (frame.type === 'PRICE_SNAPSHOT' || frame.type === 'PRICE_UPDATE') {
        this._prices.update(map => {
          const next = new Map(map);
          (frame.prices as Price[]).forEach(p => next.set(p.instrumentId, p));
          return next;
        });
        if (frame.type === 'PRICE_SNAPSHOT') this.reconnectDelay = 1000;
      }
      if (frame.type === 'PING')       this.ws?.send(JSON.stringify({ type: 'PONG' }));
      if (frame.type === 'AUTH_ERROR') this.disconnect();  // bad token — do not retry
    };

    this.ws.onclose = () => {
      const freshToken = authStore.profile()?.token;
      if (freshToken) {
        this.reconnectTimer = setTimeout(() => this.connect(this.currentWsUrl, freshToken), this.reconnectDelay);
        this.reconnectDelay = Math.min(this.reconnectDelay * 2, 30_000);
      }
    };
  }

  reconnect(newToken: string): void {
    this.disconnect();
    this.connect(this.currentWsUrl, newToken);
  }

  disconnect(): void {
    clearTimeout(this.reconnectTimer);
    this.ws?.close();
    this.ws = null;
  }
}
```

### Portfolio Store (`store/portfolio.store.ts`) — injectable

```typescript
@Injectable({ providedIn: 'root' })
export class PortfolioStore {
  private priceStore = inject(PriceStore);
  private readonly _portfolio = signal<ClientPortfolio | null>(null);
  private lastClientId: string | null = null;

  readonly portfolio = this._portfolio.asReadonly();
  readonly balance   = computed(() => this._portfolio()?.currBalance ?? 0);

  readonly holdingWithPL = computed((): HoldingWithPL[] => {
    const portfolio = this._portfolio();
    if (!portfolio) return [];
    const prices = this.priceStore.prices();
    return portfolio.holdings.map(h => {
      const currentBidPrice = prices.get(h.instrumentId)?.bidPrice ?? h.avgPrice;
      const unrealizedPL    = (currentBidPrice - h.avgPrice) * h.quantity;
      const unrealizedPLPct = h.avgPrice > 0
        ? (currentBidPrice - h.avgPrice) / h.avgPrice * 100
        : 0;
      return { ...h, currentBidPrice, unrealizedPL, unrealizedPLPct };
    });
  });

  load(clientId: string): void {
    this.lastClientId = clientId;
    // caller injects PortfolioService and calls load — store just holds data
  }

  setPortfolio(portfolio: ClientPortfolio): void { this._portfolio.set(portfolio); }
  refresh(): void { if (this.lastClientId) this.load(this.lastClientId); }
  clear():   void { this._portfolio.set(null); this.lastClientId = null; }
}
```

---

## Routing (`app.routes.ts`)

```typescript
export const routes: Routes = [
  { path: '', component: LandingComponent },
  { path: 'register', component: RegistrationComponent },
  {
    path: 'dashboard',
    component: DashboardShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'instruments', pathMatch: 'full' },
      { path: 'instruments',    component: InstrumentsComponent },
      { path: 'portfolio',      component: PortfolioComponent },
      { path: 'trading-history',component: TradingHistoryComponent },
      { path: 'preferences',    component: PreferencesComponent },
      { path: 'activity-report',component: ActivityReportComponent },
    ],
  },
  { path: '**', redirectTo: '' },
];
```

---

## Auth Guard and Interceptor

```typescript
// auth.guard.ts — functional
export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const tokenService = inject(TokenService);
  const token = authStore.profile()?.token;
  if (!token || tokenService.isExpired(token)) {
    authStore.logout();
    return router.createUrlTree(['/']);
  }
  return true;
};

// auth.interceptor.ts — functional
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = authStore.profile()?.token;
  if (token) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }
  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401) {
        authStore.logout();
        inject(Router).navigate(['/']);
      }
      return throwError(() => err);
    })
  );
};
```

Token refresh: in `AppComponent.ngOnInit()`, `setInterval` every 60s checks `tokenService.shouldRefresh(token)`. If true: `POST /auth/refresh` → `authStore.updateToken(newToken)` → `priceStore.reconnect(newToken)`.

---

## `app.config.ts`

```typescript
export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideAnimationsAsync(),
  ],
};
```

---

## Environment Config

```typescript
// environments/environment.ts
export const environment = {
  production: false,
  apiUrl:     'http://localhost:8080',        // Spring Boot
  wsUrl:      'ws://localhost:3001/ws/prices', // MDS WebSocket
  mdsHttpUrl: 'http://localhost:3001',         // MDS REST
};

// environments/environment.prod.ts
export const environment = {
  production: true,
  apiUrl:     'https://your-backend.railway.app',
  wsUrl:      'wss://your-backend.railway.app/ws/prices',
  mdsHttpUrl: 'https://your-mds.railway.app',
};
```

---

## Key Component Notes

### Instruments Component
- AG Grid rowData: driven by `toObservable(priceStore.prices)` subscription calling `gridApi.setGridOption('rowData', ...)`
- `getRowId: params => params.data.instrumentId` — required for delta updates + flash animation correctness
- `BreakpointObserver` controls column visibility: desktop shows all 8 columns; mobile shows Instrument, Last, Buy, Sell
- AG Grid theme class toggled based on current theme: `ag-theme-alpine-dark` / `ag-theme-alpine`
- Buy/Sell buttons disabled when `price.marketOpen === false`

### TradingFormDialog
- Live price via `computed(() => priceStore.prices().get(instrumentId) ?? fallback)`
- Fee display (from BUSINESS_LOGIC.md):
  - BUY: `total = qty × ask × 1.001`, `fee = qty × ask × 0.001`
  - SELL: `net = qty × bid × 0.999`, `fee = qty × bid × 0.001`
- Order built with `crypto.randomUUID()` for `orderId`
- `orderType: 'MARKET' | 'LIMIT'` — required field; `targetPrice: null` for MARKET
- On error: display error code inline, keep dialog open

### Portfolio Component
- `holdingWithPL` computed signal updates live as WebSocket prices arrive — no manual refresh needed
- unrealizedPL formula: `(currentBidPrice − holding.avgPrice) × holding.quantity`
- `holding.avgPrice` includes buy fee (set server-side) — do not adjust client-side
- Chart.js: register only `ArcElement, DoughnutController, Tooltip, Legend` (tree-shakeable)

### Activity Report
- **Excel export uses SheetJS (`xlsx`) — NOT `gridApi.exportDataAsExcel()` which is AG Grid Enterprise**
  ```typescript
  import * as XLSX from 'xlsx';
  exportToExcel(data: object[], filename: string): void {
    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Report');
    XLSX.writeFile(wb, `${filename}_${new Date().toISOString().slice(0, 10)}.xlsx`);
  }
  ```

---

## Financial Logic (Do Not Get These Wrong)

From `docs/BUSINESS_LOGIC.md`:

- BUY cashValue = `quantity × askPrice × 1.001`
- SELL cashValue = `quantity × bidPrice × 0.999`
- unrealizedPL = `(currentBidPrice − holding.avgPrice) × holding.quantity`
  - Use **bidPrice** — what you could get if you sold right now
- `holding.avgPrice` already includes the buy fee — set by backend on trade execution
- LIMIT BUY: execute only if `askPrice ≤ targetPrice`
- LIMIT SELL: execute only if `bidPrice ≥ targetPrice`
- MARKET orders require `marketOpen === true` — disable Buy/Sell buttons when closed

---

## What Was Removed from the Old Frontend

| Old | New |
|-----|-----|
| Angular Material | SPARTAN UI + Tailwind |
| NgModule declarations | Standalone components |
| `localStorage` for token | `sessionStorage` (`tn-session`) |
| `localStorage` for prices | Live `PriceStore` signal |
| `crypto-js` password hash | Plain password over HTTPS |
| `ag-grid-enterprise` | `ag-grid-community` + SheetJS |
| Mid-tier URL `localhost:4000` | Spring Boot `localhost:8080` + MDS `localhost:3001` |
| `Order.token: number` | `Order.token: string` (JWT) |
| No `orderType` field | `orderType: 'MARKET' \| 'LIMIT'` required |
| BehaviorSubject stores | Angular Signals |
| `any` types throughout | Strict TypeScript interfaces |

---

## Verification Checklist

```bash
# Type check
npx tsc --noEmit          # zero errors

# Dev server
ng serve                  # http://localhost:4200 — loads in dark mode

# Production build
ng build --configuration=production   # zero errors, initial bundle < 1MB
```

### Golden Path Test (manual, requires all 3 backend services running)

1. App loads in dark mode with no flash of light theme
2. Login with dev seed credentials → JWT in sessionStorage → redirect to `/dashboard/instruments`
3. Instruments grid shows 12 rows with live bid/ask/last prices; price cells flash green/red on update
4. Execute a MARKET BUY → success toast → portfolio reflects new holding + reduced balance
5. Execute a LIMIT BUY below current ask price → `LIMIT_NOT_MET` error shown inline, dialog stays open
6. Portfolio page: holdings grid shows unrealized P&L updating live; doughnut chart shows correct allocation
7. Preferences saved with `acceptAdvisor: true` → "Get Recommendations" button → robo advisor dialog loads
8. Activity report: all 3 tabs load data; Excel export downloads valid `.xlsx` for each tab
9. Token refresh: 60s interval visible in Network tab as `POST /auth/refresh`
10. Theme toggle switches to light mode → all grids, charts, text remain readable
11. Mobile (375px viewport): bottom tab bar visible, sidebar hidden, trading dialog full-screen
12. Logout → sessionStorage cleared → WS disconnected → landing page
