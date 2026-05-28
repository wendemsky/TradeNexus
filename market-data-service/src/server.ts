import 'dotenv/config'
import http from 'http'
import app from './app.js'
import { initPriceSocket } from './ws/priceSocket.js'
import { startPriceFetcher } from './jobs/priceFetcher.js'

const PORT = parseInt(process.env.PORT ?? '3001')

const httpServer = http.createServer(app)

initPriceSocket(httpServer)
startPriceFetcher()

httpServer.listen(PORT, () => {
  console.log(`[server] MDS running on http://localhost:${PORT}`)
})
