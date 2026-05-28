import 'dotenv/config'
import yahooFinanceRaw from 'yahoo-finance2'
import axios from 'axios'
import cron from 'node-cron'
import { bondPrice } from '../lib/bondPricer.js'
import { isNYSEOpen } from '../lib/marketHours.js'
import { priceCache } from '../cache/PriceCache.js'
import { INSTRUMENT_MAP } from '../data/instruments.js'

// yahoo-finance2 exports the class (not an instance); quote lives on the prototype.
// The type declaration's &{ quote } refers to the class but it's really on instances.
interface YFQuote { symbol: string; regularMarketPrice?: number }
type YFCtor = new () => { quote(symbol: string): Promise<YFQuote> }
const yf = new (yahooFinanceRaw as unknown as YFCtor)()

const STOCK_TICKERS = ['GOOGL', 'TSLA', 'JPM', 'BRK-B', 'AAPL', 'MSFT', 'SPY']
const FRED_SERIES   = ['DGS2', 'DGS5', 'DGS10', 'DGS20', 'DGS30']
const FRED_BASE_URL = 'https://fred.stlouisfed.org/graph/fredgraph.csv'

const FRED_TO_INSTRUMENT: Record<string, string> = {
  DGS2:  'US2Y',
  DGS5:  'US5Y',
  DGS10: 'US10Y',
  DGS20: 'US20Y',
  DGS30: 'US30Y',
}

function yearsToMaturity(maturityDate: string): number {
  const now = Date.now()
  const maturity = new Date(maturityDate).getTime()
  return (maturity - now) / (1000 * 60 * 60 * 24 * 365.25)
}

async function fetchStockPrices(): Promise<void> {
  const quotes = await Promise.all(STOCK_TICKERS.map(t => yf.quote(t)))
  const timestamp = new Date().toISOString()
  const marketOpen = isNYSEOpen()

  for (const q of quotes) {
    const last = q.regularMarketPrice ?? 0
    const spread = last * 0.0005
    priceCache.set(q.symbol, {
      instrumentId: q.symbol,
      ticker: q.symbol,
      lastPrice: last,
      bidPrice:  Math.round((last - spread / 2) * 10000) / 10000,
      askPrice:  Math.round((last + spread / 2) * 10000) / 10000,
      priceTimestamp: timestamp,
      marketOpen,
    })
  }
}

async function fetchBondPrices(): Promise<void> {
  const timestamp = new Date().toISOString()

  for (const series of FRED_SERIES) {
    const resp = await axios.get<string>(
      `${FRED_BASE_URL}?id=${series}&api_key=${process.env.FRED_API_KEY}`
    )
    const lines = resp.data.trim().split('\n')
    const latest = lines[lines.length - 1].split(',')
    const yieldPct = parseFloat(latest[1]) / 100
    if (isNaN(yieldPct)) continue

    const instrumentId = FRED_TO_INSTRUMENT[series]
    const instrument = INSTRUMENT_MAP.get(instrumentId)!
    const yearsLeft = yearsToMaturity(instrument.maturityDate!)
    const midPrice = bondPrice(100, instrument.couponRate!, yieldPct, yearsLeft)
    const spread = 0.125

    priceCache.set(instrumentId, {
      instrumentId,
      ticker: series,
      lastPrice: midPrice,
      bidPrice:  Math.round((midPrice - spread / 2) * 10000) / 10000,
      askPrice:  Math.round((midPrice + spread / 2) * 10000) / 10000,
      priceTimestamp: timestamp,
      marketOpen: true,
    })
  }
}

export function startPriceFetcher(): void {
  const stockInterval = parseInt(process.env.PRICE_REFRESH_SECONDS ?? '15')

  cron.schedule(`*/${stockInterval} * * * * *`, () => {
    fetchStockPrices().catch(err => console.error('[priceFetcher] stock fetch error:', err))
  })

  cron.schedule('*/60 * * * * *', () => {
    fetchBondPrices().catch(err => console.error('[priceFetcher] bond fetch error:', err))
  })

  fetchStockPrices().catch(err => console.error('[priceFetcher] initial stock fetch error:', err))
  fetchBondPrices().catch(err => console.error('[priceFetcher] initial bond fetch error:', err))
}

export { fetchStockPrices, fetchBondPrices }
