import 'dotenv/config'
import { startPriceFetcher } from './jobs/priceFetcher.js'
import { priceCache } from './cache/PriceCache.js'

startPriceFetcher()

priceCache.on('update', (instrumentId: string) => {
  console.log(`[cache] updated: ${instrumentId}`)
})

console.log('[server] price fetcher started — watching for updates...')
