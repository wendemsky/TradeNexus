import { computed, inject, Injectable, signal } from '@angular/core';
import { ClientPortfolio, HoldingWithPL } from '../shared/models/client.models';
import { PriceStore } from './price.store';

@Injectable({ providedIn: 'root' })
export class PortfolioStore {
  private readonly priceStore = inject(PriceStore);
  private readonly _portfolio = signal<ClientPortfolio | null>(null);
  private lastClientId: string | null = null;
  private loadFn?: (clientId: string) => void;

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
        ? ((currentBidPrice - h.avgPrice) / h.avgPrice) * 100
        : 0;
      return { ...h, currentBidPrice, unrealizedPL, unrealizedPLPct };
    });
  });

  readonly totalMarketValue = computed(() =>
    this.holdingWithPL().reduce((sum, h) => sum + h.currentBidPrice * h.quantity, 0)
  );

  readonly totalNetWorth = computed(() => this.balance() + this.totalMarketValue());

  setPortfolio(portfolio: ClientPortfolio): void {
    this._portfolio.set(portfolio);
  }

  registerLoadFn(fn: (clientId: string) => void): void {
    this.loadFn = fn;
  }

  load(clientId: string): void {
    this.lastClientId = clientId;
    this.loadFn?.(clientId);
  }

  refresh(): void {
    if (this.lastClientId) {
      this.load(this.lastClientId);
    }
  }

  clear(): void {
    this._portfolio.set(null);
    this.lastClientId = null;
  }
}
