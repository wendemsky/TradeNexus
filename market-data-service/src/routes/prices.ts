import { Router } from 'express'
import { priceCache } from '../cache/PriceCache.js'
import { INSTRUMENTS, INSTRUMENT_MAP } from '../data/instruments.js'

const router = Router()

router.get('/', (req, res) => {
  const { category } = req.query
  let prices = priceCache.getAll()

  if (typeof category === 'string') {
    const ids = new Set(
      INSTRUMENTS
        .filter(i => i.categoryId === category.toUpperCase())
        .map(i => i.instrumentId)
    )
    prices = prices.filter(p => ids.has(p.instrumentId))
  }

  res.json(prices)
})

router.get('/:instrumentId/history', (req, res) => {
  const { instrumentId } = req.params

  if (!INSTRUMENT_MAP.has(instrumentId)) {
    res.status(404).json({ message: 'Instrument not found' })
    return
  }

  res.json(priceCache.getHistory(instrumentId))
})

router.get('/:instrumentId', (req, res) => {
  const { instrumentId } = req.params

  if (!INSTRUMENT_MAP.has(instrumentId)) {
    res.status(404).json({ message: 'Instrument not found' })
    return
  }

  const price = priceCache.get(instrumentId)
  if (!price) {
    res.status(404).json({ message: 'Price not yet available' })
    return
  }

  res.json(price)
})

export default router
