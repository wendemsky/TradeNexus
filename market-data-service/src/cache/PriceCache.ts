import { EventEmitter } from 'events'

export interface Price {
  instrumentId: string
  ticker: string
  lastPrice: number
  bidPrice: number
  askPrice: number
  priceTimestamp: string
  marketOpen: boolean
}

class PriceCache extends EventEmitter {
  private cache = new Map<string, Price>()
  private history = new Map<string, Price[]>()

  set(instrumentId: string, price: Price): void {
    this.cache.set(instrumentId, price)
    this.appendHistory(instrumentId, price)
    this.emit('update', instrumentId, price)
  }

  getAll(): Price[] {
    return Array.from(this.cache.values())
  }

  get(instrumentId: string): Price | undefined {
    return this.cache.get(instrumentId)
  }

  getHistory(instrumentId: string, limit = 30): Price[] {
    return (this.history.get(instrumentId) ?? []).slice(-limit)
  }

  private appendHistory(id: string, price: Price): void {
    const arr = this.history.get(id) ?? []
    arr.push(price)
    if (arr.length > 30) arr.shift()
    this.history.set(id, arr)
  }
}

export const priceCache = new PriceCache()
