import { Component, inject, OnInit, signal } from '@angular/core';
import { ColDef } from 'ag-grid-community';
import { AgGridAngular } from 'ag-grid-angular';
import * as XLSX from 'xlsx';
import { ActivityService } from '../../../core/services/activity.service';
import { authStore } from '../../../store/auth.store';
import { Holding } from '../../../shared/models/client.models';
import { Trade, TradePL } from '../../../shared/models/trade.models';

type Tab = 'holdings' | 'trades' | 'pl';

interface TabItem {
  id: Tab;
  label: string;
}

@Component({
  selector: 'tn-activity-report',
  standalone: true,
  imports: [AgGridAngular],
  templateUrl: './activity-report.component.html',
})
export class ActivityReportComponent implements OnInit {
  private readonly activityService = inject(ActivityService);

  readonly activeTab = signal<Tab>('holdings');
  readonly isLoading = signal(false);
  readonly errorMsg  = signal('');

  readonly holdings = signal<Holding[]>([]);
  readonly trades   = signal<Trade[]>([]);
  readonly pl       = signal<TradePL[]>([]);

  readonly tabs: TabItem[] = [
    { id: 'holdings', label: 'Holdings' },
    { id: 'trades',   label: 'Trade History' },
    { id: 'pl',       label: 'P&L Summary' },
  ];

  private loadedTabs = new Set<Tab>();

  readonly holdingCols: ColDef<Holding>[] = [
    { field: 'instrumentDescription', headerName: 'Instrument', flex: 2, minWidth: 140 },
    {
      field: 'categoryId', headerName: 'Category', minWidth: 80,
      cellRenderer: (p: { value: string }) => {
        const cls = p.value === 'STOCK' ? 'badge-blue' : p.value === 'GOVT' ? 'badge-amber' : 'badge-green';
        return `<span class="badge ${cls}">${p.value}</span>`;
      },
    },
    { field: 'quantity', headerName: 'Qty', minWidth: 70, type: 'numericColumn' },
    {
      field: 'avgPrice', headerName: 'Avg Cost', minWidth: 100, cellClass: 'price-cell',
      valueFormatter: p => `$${(p.value as number).toFixed(2)}`,
    },
  ];

  readonly tradeCols: ColDef<Trade>[] = [
    {
      field: 'direction', headerName: 'Type', minWidth: 80,
      cellRenderer: (p: { value: string }) =>
        p.value === 'B'
          ? '<span class="badge badge-green">BUY</span>'
          : '<span class="badge badge-red">SELL</span>',
    },
    { field: 'instrumentId',   headerName: 'Instrument', minWidth: 120 },
    { field: 'quantity',       headerName: 'Qty',         minWidth: 70,  type: 'numericColumn' },
    {
      field: 'executionPrice', headerName: 'Exec Price', minWidth: 100, cellClass: 'price-cell',
      valueFormatter: p => `$${(p.value as number).toFixed(2)}`,
    },
    {
      field: 'cashValue',      headerName: 'Cash Value', minWidth: 110, cellClass: 'price-cell',
      valueFormatter: p => `$${(p.value as number).toFixed(2)}`,
    },
    {
      field: 'executedAt',     headerName: 'Date',        minWidth: 140, sort: 'desc',
      valueFormatter: p => new Date(p.value as string).toLocaleDateString('en-US', { dateStyle: 'medium' }),
    },
  ];

  readonly plCols: ColDef<TradePL>[] = [
    { field: 'instrumentDescription', headerName: 'Instrument', flex: 2, minWidth: 140 },
    {
      field: 'categoryId', headerName: 'Category', minWidth: 80,
      cellRenderer: (p: { value: string }) => {
        const cls = p.value === 'STOCK' ? 'badge-blue' : p.value === 'GOVT' ? 'badge-amber' : 'badge-green';
        return `<span class="badge ${cls}">${p.value}</span>`;
      },
    },
    {
      field: 'realizedPL', headerName: 'Realized P&L', minWidth: 120,
      cellClass: (p) => (p.value as number) >= 0 ? 'text-positive price-cell' : 'text-negative price-cell',
      valueFormatter: p => this.plFormat(p.value as number),
    },
    {
      field: 'unrealizedPL', headerName: 'Unrealized P&L', minWidth: 120,
      cellClass: (p) => (p.value as number) >= 0 ? 'text-positive price-cell' : 'text-negative price-cell',
      valueFormatter: p => this.plFormat(p.value as number),
    },
    {
      field: 'totalPL', headerName: 'Total P&L', minWidth: 110,
      cellClass: (p) => (p.value as number) >= 0
        ? 'text-positive price-cell font-semibold'
        : 'text-negative price-cell font-semibold',
      valueFormatter: p => this.plFormat(p.value as number),
    },
  ];

  readonly defaultColDef: ColDef = { sortable: true, resizable: true, flex: 1 };

  ngOnInit(): void {
    this.loadTab('holdings');
  }

  switchTab(tab: Tab): void {
    this.activeTab.set(tab);
    this.errorMsg.set('');
    if (!this.loadedTabs.has(tab)) {
      this.loadTab(tab);
    }
  }

  get currentExportData(): object[] {
    const tab = this.activeTab();
    if (tab === 'holdings') return this.holdings() as object[];
    if (tab === 'trades')   return this.trades()   as object[];
    return this.pl() as object[];
  }

  exportToExcel(): void {
    const ws = XLSX.utils.json_to_sheet(this.currentExportData);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Report');
    XLSX.writeFile(wb, `${this.activeTab()}_${new Date().toISOString().slice(0, 10)}.xlsx`);
  }

  private loadTab(tab: Tab): void {
    const clientId = authStore.clientId();
    if (!clientId) return;
    this.isLoading.set(true);

    if (tab === 'holdings') {
      this.activityService.getHoldings(clientId).subscribe({
        next: data => { this.holdings.set(data); this.loadedTabs.add(tab); this.isLoading.set(false); },
        error: () => { this.errorMsg.set('Failed to load holdings.'); this.isLoading.set(false); },
      });
    } else if (tab === 'trades') {
      this.activityService.getTrades(clientId).subscribe({
        next: data => { this.trades.set(data.trades ?? []); this.loadedTabs.add(tab); this.isLoading.set(false); },
        error: () => { this.errorMsg.set('Failed to load trade history.'); this.isLoading.set(false); },
      });
    } else {
      this.activityService.getPL(clientId).subscribe({
        next: data => { this.pl.set(data); this.loadedTabs.add(tab); this.isLoading.set(false); },
        error: () => { this.errorMsg.set('Failed to load P&L data.'); this.isLoading.set(false); },
      });
    }
  }

  private plFormat(v: number): string {
    return `${v >= 0 ? '+' : ''}$${v.toFixed(2)}`;
  }

  get themeClass(): string {
    return document.documentElement.classList.contains('dark')
      ? 'ag-theme-alpine-dark' : 'ag-theme-alpine';
  }
}
