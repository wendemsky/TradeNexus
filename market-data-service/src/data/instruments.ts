export interface Instrument {
  instrumentId: string
  ticker: string
  categoryId: 'STOCK' | 'ETF' | 'GOVT'
  instrumentDescription: string
  couponRate?: number
  maturityDate?: string
  minQuantity: number
  maxQuantity: number
}

export const INSTRUMENTS: Instrument[] = [
  { instrumentId: 'GOOGL',  ticker: 'GOOGL',  categoryId: 'STOCK', instrumentDescription: 'Alphabet Inc. Class A',          minQuantity: 1, maxQuantity: 1000 },
  { instrumentId: 'TSLA',   ticker: 'TSLA',   categoryId: 'STOCK', instrumentDescription: 'Tesla Inc.',                     minQuantity: 1, maxQuantity: 1000 },
  { instrumentId: 'JPM',    ticker: 'JPM',    categoryId: 'STOCK', instrumentDescription: 'JPMorgan Chase & Co.',            minQuantity: 1, maxQuantity: 1000 },
  { instrumentId: 'BRK-B',  ticker: 'BRK-B',  categoryId: 'STOCK', instrumentDescription: 'Berkshire Hathaway Class B',      minQuantity: 1, maxQuantity: 1000 },
  { instrumentId: 'AAPL',   ticker: 'AAPL',   categoryId: 'STOCK', instrumentDescription: 'Apple Inc.',                     minQuantity: 1, maxQuantity: 1000 },
  { instrumentId: 'MSFT',   ticker: 'MSFT',   categoryId: 'STOCK', instrumentDescription: 'Microsoft Corp.',                minQuantity: 1, maxQuantity: 1000 },
  { instrumentId: 'SPY',    ticker: 'SPY',    categoryId: 'ETF',   instrumentDescription: 'SPDR S&P 500 ETF Trust',          minQuantity: 1, maxQuantity: 1000 },
  { instrumentId: 'US2Y',   ticker: 'DGS2',   categoryId: 'GOVT',  instrumentDescription: 'US Treasury 2-Year Note',  couponRate: 0.0475, maturityDate: '2027-03-31', minQuantity: 1, maxQuantity: 500 },
  { instrumentId: 'US5Y',   ticker: 'DGS5',   categoryId: 'GOVT',  instrumentDescription: 'US Treasury 5-Year Note',  couponRate: 0.0425, maturityDate: '2030-03-31', minQuantity: 1, maxQuantity: 500 },
  { instrumentId: 'US10Y',  ticker: 'DGS10',  categoryId: 'GOVT',  instrumentDescription: 'US Treasury 10-Year Note', couponRate: 0.0400, maturityDate: '2035-03-31', minQuantity: 1, maxQuantity: 500 },
  { instrumentId: 'US20Y',  ticker: 'DGS20',  categoryId: 'GOVT',  instrumentDescription: 'US Treasury 20-Year Bond', couponRate: 0.0425, maturityDate: '2045-03-31', minQuantity: 1, maxQuantity: 500 },
  { instrumentId: 'US30Y',  ticker: 'DGS30',  categoryId: 'GOVT',  instrumentDescription: 'US Treasury 30-Year Bond', couponRate: 0.0450, maturityDate: '2055-03-31', minQuantity: 1, maxQuantity: 500 },
]

export const INSTRUMENT_MAP = new Map(INSTRUMENTS.map(i => [i.instrumentId, i]))
