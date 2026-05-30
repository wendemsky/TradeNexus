import { Component, inject, OnInit, signal } from '@angular/core';
import { ColDef } from 'ag-grid-community';
import { AgGridAngular } from 'ag-grid-angular';
import { TradeService } from '../../../core/services/trade.service';
import { authStore } from '../../../store/auth.store';
import { Trade } from '../../../shared/models/trade.models';

@Component({
  selector: 'tn-trading-history',
  standalone: true,
  imports: [AgGridAngular],
  templateUrl: './trading-history.component.html',
})
export class TradingHistoryComponent implements OnInit {
  private readonly tradeService = inject(TradeService);

  readonly isLoading = signal(true);
  readonly trades    = signal<Trade[]>([]);
  readonly error     = signal('');

  readonly columnDefs: ColDef<Trade>[] = [
    {
      field: 'direction',
      headerName: 'Type',
      minWidth: 80,
      cellRenderer: (p: { value: string }) =>
        p.value === 'B'
          ? '<span class="badge badge-green">BUY</span>'
          : '<span class="badge badge-red">SELL</span>',
    },
    {
      field: 'order.orderType',
      headerName: 'Order',
      minWidth: 80,
      cellRenderer: (p: { value: string }) =>
        `<span class="badge badge-gray">${p.value}</span>`,
    },
    { field: 'instrumentId',    headerName: 'Instrument',  minWidth: 120 },
    { field: 'quantity',        headerName: 'Qty',         minWidth: 70, type: 'numericColumn' },
    {
      field: 'executionPrice',
      headerName: 'Exec Price',
      minWidth: 100,
      cellClass: 'price-cell',
      valueFormatter: p => `$${(p.value as number).toFixed(2)}`,
    },
    {
      field: 'cashValue',
      headerName: 'Cash Value',
      minWidth: 110,
      cellClass: 'price-cell font-semibold',
      valueFormatter: p => `$${(p.value as number).toFixed(2)}`,
    },
    {
      field: 'executedAt',
      headerName: 'Executed At',
      minWidth: 160,
      sort: 'desc',
      valueFormatter: p =>
        new Date(p.value as string).toLocaleString('en-US', {
          dateStyle: 'medium',
          timeStyle: 'short',
        }),
    },
  ];

  readonly defaultColDef: ColDef<Trade> = {
    sortable: true, resizable: true, flex: 1,
  };

  ngOnInit(): void {
    const clientId = authStore.clientId();
    if (!clientId) return;

    this.tradeService.getTradeHistory(clientId).subscribe({
      next: history => {
        this.trades.set(history.trades ?? []);
        this.isLoading.set(false);
      },
      error: () => {
        this.error.set('Failed to load trade history.');
        this.isLoading.set(false);
      },
    });
  }

  get themeClass(): string {
    return document.documentElement.classList.contains('dark')
      ? 'ag-theme-alpine-dark' : 'ag-theme-alpine';
  }
}
