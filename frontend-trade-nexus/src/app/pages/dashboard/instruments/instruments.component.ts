import {
  Component, inject, OnDestroy, OnInit, signal
} from '@angular/core';
import { Dialog } from '@angular/cdk/dialog';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { toObservable } from '@angular/core/rxjs-interop';
import { ColDef, GridApi, GridReadyEvent } from 'ag-grid-community';
import { AgGridAngular } from 'ag-grid-angular';
import { Subscription } from 'rxjs';
import { PriceStore } from '../../../store/price.store';
import { PortfolioStore } from '../../../store/portfolio.store';
import { authStore } from '../../../store/auth.store';
import { Price } from '../../../shared/models/price.models';
import { PriceFlashCellComponent } from '../../../shared/components/price-flash-cell/price-flash-cell.component';
import { TradingFormDialogComponent, TradingFormDialogData } from '../../../shared/components/trading-form-dialog/trading-form-dialog.component';

@Component({
  selector: 'tn-instruments',
  standalone: true,
  imports: [AgGridAngular],
  templateUrl: './instruments.component.html',
})
export class InstrumentsComponent implements OnInit, OnDestroy {
  private readonly priceStore      = inject(PriceStore);
  private readonly portfolioStore  = inject(PortfolioStore);
  private readonly dialog          = inject(Dialog);
  private readonly breakpoint      = inject(BreakpointObserver);
  private gridApi?: GridApi<Price>;
  private subs = new Subscription();

  readonly isLoading = signal(true);
  private readonly prices$ = toObservable(this.priceStore.prices);

  columnDefs: ColDef<Price>[] = [];
  defaultColDef: ColDef<Price> = {
    sortable: true,
    resizable: true,
    suppressMovable: false,
    flex: 1,
    minWidth: 80,
  };

  ngOnInit(): void {
    this.buildColumnDefs(false);

    this.subs.add(
      this.breakpoint.observe([Breakpoints.XSmall, Breakpoints.Small]).subscribe(state => {
        this.buildColumnDefs(state.matches);
        if (this.gridApi) {
          this.gridApi.setGridOption('columnDefs', this.columnDefs);
        }
      })
    );

    this.subs.add(
      this.prices$.subscribe(prices => {
        if (this.gridApi) {
          this.gridApi.setGridOption('rowData', Array.from(prices.values()));
          this.isLoading.set(false);
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  onGridReady(event: GridReadyEvent<Price>): void {
    this.gridApi = event.api;
    const current = Array.from(this.priceStore.prices().values());
    if (current.length) {
      this.gridApi.setGridOption('rowData', current);
      this.isLoading.set(false);
    }
  }

  getRowId = (params: { data: Price }) => params.data.instrumentId;

  private buildColumnDefs(isMobile: boolean): void {
    const priceCol = (field: keyof Price, header: string): ColDef<Price> => ({
      field,
      headerName: header,
      cellRenderer: PriceFlashCellComponent,
      cellClass: 'price-cell',
      minWidth: 90,
      hide: isMobile && field !== 'lastPrice',
    });

    this.columnDefs = [
      {
        field:      'instrument.instrumentDescription',
        headerName: 'Instrument',
        minWidth:   160,
        flex:       2,
      },
      {
        field:      'instrument.categoryId',
        headerName: 'Category',
        hide:       isMobile,
        minWidth:   80,
        cellRenderer: (p: { value: string }) => {
          const cls = p.value === 'STOCK' ? 'badge-blue'
                    : p.value === 'GOVT'  ? 'badge-amber'
                    : 'badge-green';
          return `<span class="badge ${cls}">${p.value}</span>`;
        },
      },
      { ...priceCol('bidPrice', 'Bid'),  hide: isMobile },
      { ...priceCol('askPrice', 'Ask'),  hide: isMobile },
      { ...priceCol('lastPrice', 'Last') },
      {
        field:      'marketOpen',
        headerName: 'Market',
        hide:       isMobile,
        minWidth:   80,
        cellRenderer: (p: { value: boolean }) =>
          p.value
            ? '<span class="badge badge-green">OPEN</span>'
            : '<span class="badge badge-red">CLOSED</span>',
      },
      {
        headerName: 'Buy',
        minWidth:   70,
        sortable:   false,
        cellRenderer: (p: { data: Price }) => this.actionButton(p.data, 'B'),
        onCellClicked: p => p.data && this.openTrade(p.data, 'B'),
      },
      {
        headerName: 'Sell',
        minWidth:   70,
        sortable:   false,
        cellRenderer: (p: { data: Price }) => this.actionButton(p.data, 'S'),
        onCellClicked: p => p.data && this.openTrade(p.data, 'S'),
      },
    ];
  }

  private actionButton(price: Price, direction: 'B' | 'S'): string {
    const disabled = !price?.marketOpen;
    const cls = direction === 'B'
      ? 'btn btn-success btn-sm'
      : 'btn btn-danger btn-sm';
    const label = direction === 'B' ? 'Buy' : 'Sell';
    return `<button class="${cls}" ${disabled ? 'disabled' : ''}>${label}</button>`;
  }

  openTrade(price: Price, direction: 'B' | 'S'): void {
    if (!price.marketOpen) return;
    const isMobile = window.innerWidth < 1024;
    const ref = this.dialog.open<boolean>(TradingFormDialogComponent, {
      data: { price, direction } satisfies TradingFormDialogData,
      panelClass: isMobile ? 'fullscreen-dialog-panel' : 'dialog-panel',
      width:  isMobile ? '100vw' : '480px',
      height: isMobile ? '100vh' : undefined,
      maxWidth: '100vw',
    });

    ref.closed.subscribe(success => {
      if (success) {
        const clientId = authStore.clientId();
        if (clientId) this.portfolioStore.load(clientId);
      }
    });
  }

  get themeClass(): string {
    return document.documentElement.classList.contains('dark')
      ? 'ag-theme-alpine-dark'
      : 'ag-theme-alpine';
  }
}
