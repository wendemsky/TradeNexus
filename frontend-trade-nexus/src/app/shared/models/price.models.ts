export interface Instrument {
  instrumentId: string;
  ticker: string;
  categoryId: 'STOCK' | 'GOVT' | 'ETF';
  instrumentDescription: string;
  maxQuantity: number;
  minQuantity: number;
  couponRate?: number;
  maturityDate?: string;
}

export interface Price {
  instrumentId: string;
  ticker: string;
  askPrice: number;
  bidPrice: number;
  lastPrice: number;
  priceTimestamp: string;
  marketOpen: boolean;
  instrument: Instrument;
}

export interface MarketStatus {
  marketOpen: boolean;
  timezone: string;
  currentTime: string;
  nextOpenAt: string | null;
  nextCloseAt: string | null;
}
