import {
  AfterViewInit, Component, computed, effect, ElementRef, inject, OnDestroy, ViewChild
} from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { Dialog } from '@angular/cdk/dialog';
import { ColDef, GridApi, GridReadyEvent } from 'ag-grid-community';
import { AgGridAngular } from 'ag-grid-angular';
import { Chart, ArcElement, DoughnutController, Tooltip, Legend } from 'chart.js';
import { PortfolioStore } from '../../../store/portfolio.store';
import { authStore } from '../../../store/auth.store';
import { HoldingWithPL } from '../../../shared/models/client.models';
import { PriceFlashCellComponent } from '../../../shared/components/price-flash-cell/price-flash-cell.component';
import { TradingFormDialogComponent, TradingFormDialogData } from '../../../shared/components/trading-form-dialog/trading-form-dialog.component';
import { PriceStore } from '../../../store/price.store';

Chart.register(ArcElement, DoughnutController, Tooltip, Legend);

const CHART_COLORS = [
  '#3b82f6','#22c55e','#f59e0b','#ef4444','#8b5cf6',
  '#06b6d4','#f97316','#ec4899','#14b8a6','#84cc16',
  '#a855f7','#0ea5e9',
];

@Component({
  selector: 'tn-portfolio',
  standalone: true,
  imports: [AgGridAngular, CurrencyPipe],
  templateUrl: './portfolio.component.html',
})
export class PortfolioComponent implements AfterViewInit, OnDestroy {
  private readonly portfolioStore = inject(PortfolioStore);
  private readonly priceStore     = inject(PriceStore);
  private readonly dialog         = inject(Dialog);
  private gridApi?: GridApi<HoldingWithPL>;
  private chart?: Chart;

  @ViewChild('doughnutCanvas') canvasRef?: ElementRef<HTMLCanvasElement>;

  readonly balance      = this.portfolioStore.balance;
  readonly netWorth     = this.portfolioStore.totalNetWorth;
  readonly holdingsPL   = this.portfolioStore.holdingWithPL;

  readonly columnDefs: ColDef<HoldingWithPL>[] = [
    { field: 'instrumentDescription', headerName: 'Instrument', flex: 2, minWidth: 140 },
    {
      field: 'categoryId', headerName: 'Category', minWidth: 80,
      cellRenderer: (p: { value: string }) => {
        const cls = p.value === 'STOCK' ? 'badge-blue'
                  : p.value === 'GOVT'  ? 'badge-amber'
                  : 'badge-green';
        return `<span class="badge ${cls}">${p.value}</span>`;
      },
    },
    { field: 'quantity',  headerName: 'Qty',       minWidth: 70,  type: 'numericColumn' },
    { field: 'avgPrice',  headerName: 'Avg Cost',  minWidth: 100, cellClass: 'price-cell', valueFormatter: p => `$${(p.value as number).toFixed(2)}` },
    { field: 'currentBidPrice', headerName: 'Current Bid', minWidth: 100, cellRenderer: PriceFlashCellComponent },
    {
      field: 'unrealizedPL',
      headerName: 'Unrealized P&L',
      minWidth: 120,
      cellClass: (p) => (p.value as number) >= 0 ? 'text-positive price-cell' : 'text-negative price-cell',
      valueFormatter: p => `${(p.value as number) >= 0 ? '+' : ''}$${(p.value as number).toFixed(2)}`,
    },
    {
      field: 'unrealizedPLPct',
      headerName: 'P&L %',
      minWidth: 90,
      cellClass: (p) => (p.value as number) >= 0 ? 'text-positive price-cell' : 'text-negative price-cell',
      valueFormatter: p => `${(p.value as number) >= 0 ? '+' : ''}${(p.value as number).toFixed(2)}%`,
    },
  ];

  readonly defaultColDef: ColDef<HoldingWithPL> = {
    sortable: true, resizable: true, flex: 1,
  };

  constructor() {
    effect(() => {
      const holdings = this.holdingsPL();
      if (this.gridApi) {
        this.gridApi.setGridOption('rowData', holdings);
      }
      this.updateChart(holdings);
    });
  }

  ngAfterViewInit(): void {
    this.initChart();
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
  }

  onGridReady(event: GridReadyEvent<HoldingWithPL>): void {
    this.gridApi = event.api;
    this.gridApi.setGridOption('rowData', this.holdingsPL());
  }

  getRowId = (p: { data: HoldingWithPL }) => p.data.instrumentId;

  private initChart(): void {
    if (!this.canvasRef) return;
    this.chart = new Chart(this.canvasRef.nativeElement, {
      type: 'doughnut',
      data: { labels: [], datasets: [{ data: [], backgroundColor: CHART_COLORS }] },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { position: 'right', labels: { color: '#94a3b8', boxWidth: 12, padding: 12 } } },
        cutout: '65%',
      },
    });
    this.updateChart(this.holdingsPL());
  }

  private updateChart(holdings: HoldingWithPL[]): void {
    if (!this.chart) return;
    const labels = [...holdings.map(h => h.instrumentDescription), 'Cash'];
    const values = [...holdings.map(h => h.currentBidPrice * h.quantity), this.balance()];
    this.chart.data.labels = labels;
    this.chart.data.datasets[0].data = values;
    this.chart.update('none');
  }

  get themeClass(): string {
    return document.documentElement.classList.contains('dark')
      ? 'ag-theme-alpine-dark' : 'ag-theme-alpine';
  }
}
