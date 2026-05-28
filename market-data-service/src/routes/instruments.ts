import { Router } from 'express'
import { INSTRUMENTS } from '../data/instruments.js'

const router = Router()

router.get('/', (_req, res) => {
  res.json(INSTRUMENTS)
})

export default router
