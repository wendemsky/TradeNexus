import { priceCache } from '../src/cache/PriceCache.js'

// Stub axios before importing priceFetcher
import axios from 'axios'

describe('priceFetcher (bond fetch)', () => {
  let axiosGetSpy: jasmine.Spy

  beforeEach(() => {
    axiosGetSpy = spyOn(axios, 'get')
  })

  it('sets bond price in cache from FRED CSV response', async () => {
    const csvResponse = 'DATE,DGS10\n2025-05-27,4.45'
    axiosGetSpy.and.returnValue(Promise.resolve({ data: csvResponse }))

    const { fetchBondPrices } = await import('../src/jobs/priceFetcher.js')
    await fetchBondPrices()

    const price = priceCache.get('US10Y')
    expect(price).toBeDefined()
    expect(price!.instrumentId).toBe('US10Y')
    expect(price!.ticker).toBe('DGS10')
    expect(price!.lastPrice).toBeGreaterThan(0)
    expect(price!.bidPrice).toBeLessThan(price!.lastPrice)
    expect(price!.askPrice).toBeGreaterThan(price!.lastPrice)
    expect(price!.marketOpen).toBeTrue()
  })

  it('bid/ask spread for bonds is exactly 0.125 total (0.0625 per side)', async () => {
    const csvResponse = 'DATE,DGS10\n2025-05-27,4.00'
    axiosGetSpy.and.returnValue(Promise.resolve({ data: csvResponse }))

    const { fetchBondPrices } = await import('../src/jobs/priceFetcher.js')
    await fetchBondPrices()

    const price = priceCache.get('US10Y')!
    const spread = price.askPrice - price.bidPrice
    expect(Math.round(spread * 10000) / 10000).toBeCloseTo(0.125, 3)
  })

  it('handles FRED returning "." (no data) gracefully', async () => {
    const csvResponse = 'DATE,DGS10\n2025-05-26,.'
    axiosGetSpy.and.returnValue(Promise.resolve({ data: csvResponse }))

    const { fetchBondPrices } = await import('../src/jobs/priceFetcher.js')

    // NaN yield should not throw but may produce NaN price — test that it doesn't crash
    await expectAsync(fetchBondPrices()).not.toBeRejected()
  })
})

describe('PriceCache', () => {
  it('emits update event when price is set', (done) => {
    priceCache.once('update', (id: string) => {
      expect(id).toBe('TEST')
      done()
    })
    priceCache.set('TEST', {
      instrumentId: 'TEST', ticker: 'TEST',
      lastPrice: 100, bidPrice: 99.975, askPrice: 100.025,
      priceTimestamp: new Date().toISOString(), marketOpen: true,
    })
  })

  it('keeps rolling history capped at 30 entries', () => {
    for (let i = 0; i < 35; i++) {
      priceCache.set('HIST_TEST', {
        instrumentId: 'HIST_TEST', ticker: 'HIST_TEST',
        lastPrice: i, bidPrice: i - 0.01, askPrice: i + 0.01,
        priceTimestamp: new Date().toISOString(), marketOpen: true,
      })
    }
    expect(priceCache.getHistory('HIST_TEST').length).toBe(30)
  })

  it('returns empty array for unknown instrument history', () => {
    expect(priceCache.getHistory('NONEXISTENT')).toEqual([])
  })
})
