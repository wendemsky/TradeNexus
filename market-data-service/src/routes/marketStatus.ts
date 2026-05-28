import { Router } from 'express'
import { DateTime } from 'luxon'
import { isNYSEOpen, secondsUntilNextOpen } from '../lib/marketHours.js'

const TZ = 'America/New_York'

const router = Router()

router.get('/', (_req, res) => {
  const marketOpen = isNYSEOpen()
  const now = new Date().toISOString()

  let nextOpenAt: string | null = null
  let nextCloseAt: string | null = null

  if (marketOpen) {
    const closeToday = DateTime.now().setZone(TZ).set({ hour: 16, minute: 0, second: 0, millisecond: 0 })
    nextCloseAt = closeToday.toUTC().toISO()
  } else {
    const secondsUntil = secondsUntilNextOpen()
    nextOpenAt = new Date(Date.now() + secondsUntil * 1000).toISOString()
  }

  res.json({ marketOpen, timezone: TZ, currentTime: now, nextOpenAt, nextCloseAt })
})

export default router
