# Frontend Service — Implementation Spec

**Directory:** `frontend-trade-nexus/`
**Port:** 4200 (dev server)
**Stack:** Angular 18, TypeScript 5, Angular Material 3, AG Grid 32
**Phase:** 5 (last — contracts must be stable before UI is built)
**Branch:** `feature/frontend/angular18`

---

## Responsibility

Complete rewrite from scratch. The old Angular 15 codebase is deleted entirely (all NgModules, services, components). The new project:
- Standalone components throughout (no NgModules)
- Angular Signals for reactive state (replaces manual RxJS BehaviorSubjects)
- WebSocket consumer for live prices via `PriceStore`
- Professional dark trading terminal aesthetic (Angular Material 3)
- JWT auth with automatic token refresh

---

## Project Bootstrap

```bash
# From repo root — replace existing directory
rm -rf frontend-trade-nexus

ng new frontend-trade-nexus \
  --routing=true \
  --style=scss \
  --standalone=true \
  --skip-git=true

cd frontend-trade-nexus
ng add @angular/material   # choose custom theme, dark mode

# Additional packages
npm install ag-grid-angular ag-grid-community ag-grid-enterprise
npm install lightweight-charts
npm install chart.js
npm install @auth0/angular-jwt   # or just use jwt-decode directly
npm install jwt-decode
```

---

## Project Structure

```
frontend-trade-nexus/src/app/
  core/
    auth/
      auth.guard.ts             # canActivate — checks JWT expiry from AuthStore
      auth.interceptor.ts       # HttpInterceptorFn — adds Authorization header
      token.service.ts          # sessionStorage get/set/remove token helpers
    services/
      client.service.ts         # register, login, refresh, verify-email
      instrument.service.ts     # GET /instruments
      price.service.ts          # REST fallback GET /trade/live-prices
      portfolio.service.ts      # GET /portfolio/client/:id
      preferences.service.ts    # GET/POST/PUT /client-preferences
      trade.service.ts          # POST /trade/execute-trade
      trade-history.service.ts  # GET /trade/trade-history/:id
      robo-advisor.service.ts   # POST /trade/suggest-buy|sell
      activity-report.service.ts
  store/
    auth.store.ts               # ClientProfile signal + derived isAdmin, clientId
    price.store.ts              # Map<instrumentId, Price> signal — WS consumer
    portfolio.store.ts          # ClientPortfolio signal — refreshed after trades
  pages/
    auth/
      landing-page/             # Login form + hero section
        landing-page.component.ts
        landing-page.component.html
        landing-page.component.scss
      registration/             # 3-step stepper
        registration.component.ts
        registration.component.html
        registration.component.scss
    dashboard/
      dashboard.component.ts    # Shell: sidebar nav + <router-outlet>
      instruments/
        instruments.component.ts    # AG Grid price list — live from priceStore
      portfolio/
        portfolio.component.ts      # Holdings grid + portfolio chart
      trading-history/
        trading-history.component.ts
      preferences/
        preferences.component.ts    # Robo advisor preferences form
      activity-report/
        activity-report.component.ts
  shared/
    components/
      trading-form-dialog/          # Buy/Sell modal dialog
        trading-form-dialog.component.ts
      robo-advisor-dialog/          # Robo advisor modal
        robo-advisor-dialog.component.ts
      price-flash-cell/             # AG Grid cell renderer — flashes on price change
        price-flash-cell.component.ts
      confirm-dialog/               # Generic confirm dialog
    models/                         # All TypeScript interfaces (matches API_CONTRACTS.md)
      client.model.ts
      instrument.model.ts
      price.model.ts
      trade.model.ts
      holding.model.ts
      preferences.model.ts
    validators/
      password.validator.ts         # Strength: upper + lower + number
      id.validator.ts               # Aadhar / PAN / SSN format validators
  app.routes.ts                     # Route config
  app.config.ts                     # ApplicationConfig (no NgModules)
environments/
  environment.ts
  environment.prod.ts
```

---

## Auth Store (`src/app/store/auth.store.ts`)

```typescript
import { signal, computed } from '@angular/core'
import { jwtDecode } from 'jwt-decode'

interface ClientSession {
  client: Client
  token: string
}

const _session = signal<ClientSession | null>(null)

export const authStore = {
  session: _session.asReadonly(),
  clientId: computed(() => _session()?.client.clientId ?? null),
  isAdmin: computed(() => _session()?.client.isAdmin ?? false),
  token: computed(() => _session()?.token ?? null),

  setSession(profile: ClientProfile) {
    _session.set({ client: profile.client, token: profile.token })
    sessionStorage.setItem('token', profile.token)
  },

  clearSession() {
    _session.set(null)
    sessionStorage.removeItem('token')
  },

  isTokenExpired(): boolean {
    const token = _session()?.token
    if (!token) return true
    try {
      const { exp } = jwtDecode<{ exp: number }>(token)
      return Date.now() >= exp * 1000
    } catch {
      return true
    }
  },

  shouldRefresh(): boolean {
    const token = _session()?.token
    if (!token) return false
    try {
      const { exp } = jwtDecode<{ exp: number }>(token)
      return Date.now() >= (exp * 1000) - 5 * 60_000  // 5 min before expiry
    } catch {
      return false
    }
  }
}
```

---

## Price Store (`src/app/store/price.store.ts`)

```typescript
import { Injectable } from '@angular/core'
import { signal, computed } from '@angular/core'

@Injectable({ providedIn: 'root' })
export class PriceStore {
  private readonly _prices = signal<Map<string, Price>>(new Map())
  private ws: WebSocket | null = null
  private reconnectDelay = 1000

  readonly prices = this._prices.asReadonly()
  readonly priceList = computed(() => Array.from(this._prices().values()))

  getPrice(instrumentId: string) {
    return computed(() => this._prices().get(instrumentId))
  }

  connect(wsUrl: string) {
    this.ws = new WebSocket(wsUrl)

    this.ws.onmessage = (event) => {
      const frame = JSON.parse(event.data)
      if (frame.type === 'PRICE_SNAPSHOT' || frame.type === 'PRICE_UPDATE') {
        this._prices.update(map => {
          const next = new Map(map)
          frame.prices.forEach((p: Price) => next.set(p.instrumentId, p))
          return next
        })
      }
      if (frame.type === 'PING') this.ws?.send(JSON.stringify({ type: 'PONG' }))
    }

    this.ws.onclose = () => {
      setTimeout(() => this.connect(wsUrl), this.reconnectDelay)
      this.reconnectDelay = Math.min(this.reconnectDelay * 2, 30_000)
    }
  }

  disconnect() {
    this.ws?.close()
    this.ws = null
  }
}
```

`PriceStore.connect()` is called once at app startup in `AppComponent.ngOnInit()`.

---

## Portfolio Store (`src/app/store/portfolio.store.ts`)

```typescript
@Injectable({ providedIn: 'root' })
export class PortfolioStore {
  private readonly _portfolio = signal<ClientPortfolio | null>(null)

  readonly portfolio = this._portfolio.asReadonly()
  readonly holdings = computed(() => this._portfolio()?.holdings ?? [])
  readonly balance = computed(() => this._portfolio()?.currBalance ?? 0)

  // Unrealized P&L per holding — computed from priceStore
  holdingWithPL(priceStore: PriceStore) {
    return computed(() => this.holdings().map(h => ({
      ...h,
      currentBid: priceStore.prices().get(h.instrumentId)?.bidPrice ?? h.avgPrice,
      unrealizedPL: ((priceStore.prices().get(h.instrumentId)?.bidPrice ?? h.avgPrice) - h.avgPrice) * h.quantity
    })))
  }

  setPortfolio(portfolio: ClientPortfolio) { this._portfolio.set(portfolio) }
  clear() { this._portfolio.set(null) }
}
```

---

## Routing (`app.routes.ts`)

```typescript
export const routes: Routes = [
  { path: '', component: LandingPageComponent },
  { path: 'register', component: RegistrationComponent },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'instruments', pathMatch: 'full' },
      { path: 'instruments', component: InstrumentsComponent },
      { path: 'portfolio', component: PortfolioComponent },
      { path: 'trading-history', component: TradingHistoryComponent },
      { path: 'preferences', component: PreferencesComponent },
      { path: 'activity-report', component: ActivityReportComponent },
    ]
  },
  { path: '**', redirectTo: '' }
]
```

---

## Auth Guard and Interceptor

```typescript
// auth.guard.ts
export const authGuard: CanActivateFn = () => {
  const router = inject(Router)
  if (authStore.isTokenExpired()) {
    authStore.clearSession()
    router.navigate(['/'])
    return false
  }
  return true
}

// auth.interceptor.ts
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = authStore.token()
  if (token) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
  }
  return next(req).pipe(
    catchError(err => {
      if (err.status === 401) {
        authStore.clearSession()
        inject(Router).navigate(['/'])
      }
      return throwError(() => err)
    })
  )
}
```

Token refresh: In `AuthService`, a scheduled interval checks `authStore.shouldRefresh()` every 60s. If within 5 minutes of expiry, calls `POST /client/refresh-token`.

---

## Key UI Components

### Instruments Component (price list)
- AG Grid Community with live data from `PriceStore`
- Columns: Description, Category, Bid, Ask, Last Price, Change %, Buy button, Sell button
- Cells flash green (price up) or red (price down) using `agAnimateShowChangeCellRenderer` or custom `PriceFlashCellComponent`
- Row click opens instrument detail drawer with `lightweight-charts` sparkline (last 30 days from FIPS history endpoint — future phase)
- Buy/Sell button opens `TradingFormDialog`

```typescript
@Component({ selector: 'tn-instruments', standalone: true, ... })
export class InstrumentsComponent {
  private priceStore = inject(PriceStore)

  rowData = this.priceStore.priceList  // signal → AG Grid reads it
  
  columnDefs: ColDef[] = [
    { field: 'instrument.instrumentDescription', headerName: 'Instrument' },
    { field: 'instrument.categoryId', headerName: 'Category' },
    { field: 'bidPrice', headerName: 'Bid', valueFormatter: moneyFormatter, enableCellChangeFlash: true },
    { field: 'askPrice', headerName: 'Ask', valueFormatter: moneyFormatter, enableCellChangeFlash: true },
    { field: 'lastPrice', headerName: 'Last', valueFormatter: moneyFormatter, enableCellChangeFlash: true },
    { headerName: 'Actions', cellRenderer: ActionsCellComponent },
  ]
}
```

### Trading Form Dialog
- Angular Material dialog
- Shows instrument name, current bid/ask/last price (live from priceStore signal)
- Quantity input (min/max from instrument.minQuantity/maxQuantity)
- Direction: Buy / Sell toggle
- Target price: pre-filled with current ask (buy) or bid (sell); user can adjust ±5% from market price
- Live total cost estimate: `quantity × price × 1.01`
- Submit calls `TradeService.executeTrade()`
- On success: refresh PortfolioStore; show MatSnackBar

### Portfolio Component
- Holdings AG Grid with columns: Instrument, Category, Qty, Avg Cost, Current Bid, Unrealized P&L, Unrealized P&L %
- Unrealized P&L computed from `portfolioStore.holdingWithPL(priceStore)` — updates live as prices change
- Chart.js doughnut chart: portfolio composition by market value (`currentBid × qty`)
- Balance card: current cash balance from PortfolioStore

### Activity Report Component
- Three tabs: Holdings | Trades | P&L
- Each tab is an AG Grid
- Excel export:
  ```typescript
  // AG Grid community doesn't include Excel export — use SheetJS (xlsx)
  import * as XLSX from 'xlsx'
  exportToExcel(data: any[], filename: string) {
    const ws = XLSX.utils.json_to_sheet(data)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, 'Report')
    XLSX.writeFile(wb, `${filename}.xlsx`)
  }
  ```
  Note: Do NOT use AG Grid Enterprise for Excel export. Use `SheetJS` (free, open source).

### Dashboard Shell
- Angular Material sidenav with navigation links
- Top bar: client name, balance indicator, logout button
- Price ticker bar (optional): scrolling strip of current prices

---

## Angular Material Theme (dark trading terminal)

`src/styles.scss`:
```scss
@use '@angular/material' as mat;

$dark-theme: mat.define-theme((
  color: (
    theme-type: dark,
    primary: mat.$cyan-palette,
    tertiary: mat.$green-palette,
  ),
  typography: (
    brand-family: 'Inter, monospace',
    bold-weight: 700
  ),
  density: (scale: -1)
));

:root {
  @include mat.all-component-themes($dark-theme);
  --mat-table-background-color: #0d1117;
}

body {
  background: #0d1117;
  color: #c9d1d9;
  font-family: 'Inter', monospace;
}
```

AG Grid theme: `ag-theme-alpine-dark` or custom CSS variables to match Material dark palette.

---

## `app.config.ts` (no NgModules)

```typescript
export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideAnimationsAsync(),
    provideHttpClient(withInterceptors([authInterceptor])),
    // No more AppModule
  ]
}
```

---

## Environment Config

```typescript
// environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:4000',
  wsUrl: 'ws://localhost:4000/ws/prices',
}

// environments/environment.prod.ts
export const environment = {
  production: true,
  apiUrl: 'https://your-midtier.railway.app',
  wsUrl: 'wss://your-midtier.railway.app/ws/prices',
}
```

---

## Removed from Old Frontend

- All NgModule declarations (`AppModule`, `MaterialModule`, `HomeModule`, etc.)
- `localStorage` for token storage → `sessionStorage`
- `localStorage` for prices cache → live `PriceStore` signals
- `crypto-js` password hashing → plain password sent over HTTPS to backend
- Manual `tap(data => console.log(data))` everywhere — remove debug logging
- All `any` types — replace with proper interfaces from `models/`

---

## Dependencies (`package.json` additions)

```json
{
  "dependencies": {
    "@angular/animations": "^18.0.0",
    "@angular/cdk": "^18.0.0",
    "@angular/common": "^18.0.0",
    "@angular/core": "^18.0.0",
    "@angular/forms": "^18.0.0",
    "@angular/material": "^18.0.0",
    "@angular/router": "^18.0.0",
    "ag-grid-angular": "^32.0.0",
    "ag-grid-community": "^32.0.0",
    "chart.js": "^4.4.0",
    "lightweight-charts": "^4.1.0",
    "jwt-decode": "^4.0.0",
    "xlsx": "^0.18.5",
    "rxjs": "^7.8.0",
    "zone.js": "~0.14.0"
  }
}
```

---

## Verification Checklist

```bash
# 1. Build
ng build --configuration=production
# Expect: zero TypeScript errors, zero lint errors

# 2. Type check
npx tsc --noEmit
# Expect: no errors

# 3. Dev server
ng serve
# Navigate to localhost:4200

# 4. Manual golden path test:
#    a. Register new user → redirect to dashboard
#    b. Observe price list updating every 15s without page refresh
#    c. Buy AAPL × 5 → balance decreases → AAPL appears in portfolio
#    d. Portfolio shows unrealized P&L updating live
#    e. Sell AAPL × 3 → quantity reduced in portfolio
#    f. Check trading history → two trades appear
#    g. Activity report → Holdings, Trades, P&L tabs; export each to Excel
#    h. Enable robo advisor in preferences → robo advisor dialog shows recommendations
#    i. Logout → sessionStorage cleared → redirect to landing

# 5. E2E (Cypress)
npm run cypress:run
# Covers: login, register, trade, portfolio, logout
```
