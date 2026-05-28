import { bondPrice } from '../src/lib/bondPricer'

describe('bondPrice', () => {
  it('returns face value when yield equals coupon rate', () => {
    const price = bondPrice(100, 0.04, 0.04, 10)
    expect(price).toBeCloseTo(100, 2)
  })

  it('prices below par when yield is above coupon rate', () => {
    const price = bondPrice(100, 0.04, 0.05, 10)
    expect(price).toBeLessThan(100)
  })

  it('prices above par when yield is below coupon rate', () => {
    const price = bondPrice(100, 0.05, 0.04, 10)
    expect(price).toBeGreaterThan(100)
  })

  it('returns face value at maturity (yearsToMaturity = 0)', () => {
    expect(bondPrice(100, 0.04, 0.05, 0)).toBe(100)
  })

  it('returns face value when past maturity (negative years)', () => {
    expect(bondPrice(100, 0.04, 0.05, -1)).toBe(100)
  })

  it('rounds result to 4 decimal places', () => {
    const price = bondPrice(100, 0.0475, 0.045, 1.25)
    const decimals = (price.toString().split('.')[1] ?? '').length
    expect(decimals).toBeLessThanOrEqual(4)
  })

  it('produces realistic US10Y price for a 4% coupon at 4.5% yield, 9 years remaining', () => {
    // Known benchmark: ~96 range
    const price = bondPrice(100, 0.04, 0.045, 9)
    expect(price).toBeGreaterThan(90)
    expect(price).toBeLessThan(100)
  })

  it('respects paymentsPerYear parameter (annual vs semi-annual)', () => {
    const semiAnnual = bondPrice(100, 0.04, 0.045, 10, 2)
    const annual = bondPrice(100, 0.04, 0.045, 10, 1)
    // Both should be below par, but values differ
    expect(semiAnnual).not.toEqual(annual)
    expect(semiAnnual).toBeLessThan(100)
    expect(annual).toBeLessThan(100)
  })
})
