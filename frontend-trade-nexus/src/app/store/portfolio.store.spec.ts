import { TestBed } from '@angular/core/testing';
import { computed, signal } from '@angular/core';
import { PortfolioStore } from './portfolio.store';
import { PriceStore } from './price.store';
import { ClientPortfolio } from '../shared/models/client.models';
import { Price } from '../shared/models/price.models';

const mockPricesSignal = signal(new Map<string, Price>());

const mockPriceStore = {
  prices:    mockPricesSignal.asReadonly(),
  priceList: computed(() => Array.from(mockPricesSignal().values())),
  connect:    vi.fn(),
  disconnect: vi.fn(),
  reconnect:  vi.fn(),
};

function makePrice(instrumentId: string, bid: number, ask: number): Price {
  return {
    instrumentId,
    ticker: instrumentId,
    bidPrice: bid,
    askPrice: ask,
    lastPrice: (bid + ask) / 2,
    priceTimestamp: new Date().toISOString(),
    marketOpen: true,
    instrument: {
      instrumentId,
      ticker: instrumentId,
      categoryId: 'STOCK',
      instrumentDescription: `${instrumentId} Inc`,
      maxQuantity: 100,
      minQuantity: 1,
    },
  };
}

const MOCK_PORTFOLIO: ClientPortfolio = {
  clientId: 'c-001',
  currBalance: 5000,
  holdings: [
    { instrumentId: 'AAPL', instrumentDescription: 'Apple Inc', categoryId: 'STOCK', quantity: 10, avgPrice: 150 },
    { instrumentId: 'MSFT', instrumentDescription: 'Microsoft Corp', categoryId: 'STOCK', quantity: 5, avgPrice: 300 },
  ],
};

describe('PortfolioStore', () => {
  let store: PortfolioStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        PortfolioStore,
        { provide: PriceStore, useValue: mockPriceStore },
      ],
    });
    store = TestBed.inject(PortfolioStore);
    mockPricesSignal.set(new Map());
  });

  it('portfolio is null and balance is 0 initially', () => {
    expect(store.portfolio()).toBeNull();
    expect(store.balance()).toBe(0);
  });

  it('setPortfolio sets balance from currBalance', () => {
    store.setPortfolio(MOCK_PORTFOLIO);
    expect(store.balance()).toBe(5000);
  });

  it('holdingWithPL uses live bidPrice when available', () => {
    store.setPortfolio(MOCK_PORTFOLIO);
    mockPricesSignal.set(new Map([['AAPL', makePrice('AAPL', 160, 161)]]));

    const h = store.holdingWithPL().find(x => x.instrumentId === 'AAPL')!;
    expect(h.currentBidPrice).toBe(160);
    expect(h.unrealizedPL).toBeCloseTo(100);           // (160 - 150) * 10
    expect(h.unrealizedPLPct).toBeCloseTo(6.667, 2);  // 10/150 * 100
  });

  it('holdingWithPL falls back to avgPrice when no live price', () => {
    store.setPortfolio(MOCK_PORTFOLIO);
    const h = store.holdingWithPL().find(x => x.instrumentId === 'MSFT')!;
    expect(h.currentBidPrice).toBe(300);
    expect(h.unrealizedPL).toBe(0);
    expect(h.unrealizedPLPct).toBe(0);
  });

  it('totalMarketValue sums currentBidPrice × quantity for all holdings', () => {
    store.setPortfolio(MOCK_PORTFOLIO);
    mockPricesSignal.set(new Map([
      ['AAPL', makePrice('AAPL', 200, 201)],
      ['MSFT', makePrice('MSFT', 400, 401)],
    ]));
    // 200 * 10 + 400 * 5 = 2000 + 2000 = 4000
    expect(store.totalMarketValue()).toBe(4000);
  });

  it('totalNetWorth = balance + totalMarketValue', () => {
    store.setPortfolio(MOCK_PORTFOLIO);
    mockPricesSignal.set(new Map([
      ['AAPL', makePrice('AAPL', 150, 151)],
      ['MSFT', makePrice('MSFT', 300, 301)],
    ]));
    // 5000 + (150*10 + 300*5) = 5000 + 3000 = 8000
    expect(store.totalNetWorth()).toBe(8000);
  });

  it('clear resets portfolio to null', () => {
    store.setPortfolio(MOCK_PORTFOLIO);
    store.clear();
    expect(store.portfolio()).toBeNull();
    expect(store.balance()).toBe(0);
    expect(store.holdingWithPL()).toEqual([]);
  });
});
