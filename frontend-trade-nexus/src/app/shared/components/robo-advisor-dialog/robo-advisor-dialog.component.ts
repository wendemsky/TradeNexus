import { Component, inject, OnInit, signal } from '@angular/core';
import { Dialog, DIALOG_DATA, DialogRef } from '@angular/cdk/dialog';
import { ColDef } from 'ag-grid-community';
import { AgGridAngular } from 'ag-grid-angular';
import { forkJoin } from 'rxjs';
import { RoboAdvisorService } from '../../../core/services/robo-advisor.service';
import { ClientPreferences, Holding } from '../../../shared/models/client.models';
import { Price } from '../../../shared/models/price.models';
import { TradingFormDialogComponent, TradingFormDialogData } from '../trading-form-dialog/trading-form-dialog.component';
import { PriceStore } from '../../../store/price.store';

interface RoboDialogData {
  clientId: string;
  preferences: ClientPreferences;
}

type ActiveTab = 'buy' | 'sell';

@Component({
  selector: 'tn-robo-advisor-dialog',
  standalone: true,
  imports: [AgGridAngular],
  templateUrl: './robo-advisor-dialog.component.html',
})
export class RoboAdvisorDialogComponent implements OnInit {
  readonly data       = inject<RoboDialogData>(DIALOG_DATA);
  private readonly dialogRef = inject(DialogRef);
  private readonly roboSvc   = inject(RoboAdvisorService);
  private readonly dialog    = inject(Dialog);
  private readonly priceStore = inject(PriceStore);

  readonly isLoading  = signal(true);
  readonly errorMsg   = signal('');
  readonly activeTab  = signal<ActiveTab>('buy');
  readonly buyPrices  = signal<Price[]>([]);
  readonly sellHoldings = signal<Holding[]>([]);

  readonly buyColDefs: ColDef<Price>[] = [
    { field: 'instrument.instrumentDescription', headerName: 'Instrument', flex: 2, minWidth: 140 },
    {
      field: 'instrument.categoryId', headerName: 'Category', minWidth: 80,
      cellRenderer: (p: { value: string }) => {
        const cls = p.value === 'STOCK' ? 'badge-blue' : p.value === 'GOVT' ? 'badge-amber' : 'badge-green';
        return `<span class="badge ${cls}">${p.value}</span>`;
      },
    },
    { field: 'bidPrice', headerName: 'Bid', minWidth: 100, cellClass: 'price-cell', valueFormatter: p => `$${(p.value as number).toFixed(2)}` },
    { field: 'askPrice', headerName: 'Ask', minWidth: 100, cellClass: 'price-cell', valueFormatter: p => `$${(p.value as number).toFixed(2)}` },
  ];

  readonly sellColDefs: ColDef<Holding>[] = [
    { field: 'instrumentDescription', headerName: 'Instrument', flex: 2, minWidth: 140 },
    {
      field: 'categoryId', headerName: 'Category', minWidth: 80,
      cellRenderer: (p: { value: string }) => {
        const cls = p.value === 'STOCK' ? 'badge-blue' : p.value === 'GOVT' ? 'badge-amber' : 'badge-green';
        return `<span class="badge ${cls}">${p.value}</span>`;
      },
    },
    { field: 'quantity', headerName: 'Qty',      minWidth: 70, type: 'numericColumn' },
    { field: 'avgPrice', headerName: 'Avg Cost', minWidth: 100, cellClass: 'price-cell', valueFormatter: p => `$${(p.value as number).toFixed(2)}` },
  ];

  readonly defaultColDef = { sortable: true, resizable: true, flex: 1 };

  ngOnInit(): void {
    forkJoin({
      buys:  this.roboSvc.suggestBuy(this.data.preferences),
      sells: this.roboSvc.suggestSell({ clientId: this.data.clientId, preferences: this.data.preferences }),
    }).subscribe({
      next: ({ buys, sells }) => {
        this.buyPrices.set(buys);
        this.sellHoldings.set(sells);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMsg.set('Failed to fetch recommendations.');
        this.isLoading.set(false);
      },
    });
  }

  close(): void {
    this.dialogRef.close();
  }

  onBuyRowClick(price: Price): void {
    const isMobile = window.innerWidth < 1024;
    this.dialog.open<boolean>(TradingFormDialogComponent, {
      data: { price, direction: 'B' } satisfies TradingFormDialogData,
      panelClass: isMobile ? 'fullscreen-dialog-panel' : 'dialog-panel',
      width: isMobile ? '100vw' : '480px',
      height: isMobile ? '100vh' : undefined,
      maxWidth: '100vw',
    });
  }

  onSellRowClick(holding: Holding): void {
    const livePrice = this.priceStore.prices().get(holding.instrumentId);
    if (!livePrice) return;
    const isMobile = window.innerWidth < 1024;
    this.dialog.open<boolean>(TradingFormDialogComponent, {
      data: { price: livePrice, direction: 'S' } satisfies TradingFormDialogData,
      panelClass: isMobile ? 'fullscreen-dialog-panel' : 'dialog-panel',
      width: isMobile ? '100vw' : '480px',
      height: isMobile ? '100vh' : undefined,
      maxWidth: '100vw',
    });
  }

  get themeClass(): string {
    return document.documentElement.classList.contains('dark')
      ? 'ag-theme-alpine-dark' : 'ag-theme-alpine';
  }
}
