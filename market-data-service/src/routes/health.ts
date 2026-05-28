import { Router } from 'express'
import { priceCache } from '../cache/PriceCache.js'

const router = Router()

router.get('/', (_req, res) => {
  const prices = priceCache.getAll()
  const lastUpdate = prices.reduce<string | null>((latest, p) => {
    if (!latest || p.priceTimestamp > latest) return p.priceTimestamp
    return latest
  }, null)

  res.json({
    status: 'ok',
    priceCount: prices.length,
    lastUpdate,
  })
})

export default router
