import { WebSocketServer, WebSocket } from 'ws'
import type { Server } from 'http'
import { priceCache } from '../cache/PriceCache.js'
import { verifyToken } from '../lib/jwtHelper.js'

export function initPriceSocket(httpServer: Server): void {
  const wss = new WebSocketServer({ server: httpServer, path: '/ws/prices' })

  wss.on('connection', (ws) => {
    let authenticated = false

    ws.on('message', (data) => {
      try {
        const frame = JSON.parse(data.toString()) as { type: string; token?: string }

        if (frame.type === 'AUTH') {
          try {
            verifyToken(frame.token ?? '')
            authenticated = true
            ws.send(JSON.stringify({
              type: 'PRICE_SNAPSHOT',
              prices: priceCache.getAll(),
              timestamp: new Date().toISOString(),
            }))
          } catch {
            ws.send(JSON.stringify({ type: 'AUTH_ERROR', message: 'Invalid token' }))
            ws.close()
          }
          return
        }

        if (frame.type === 'PONG') return
      } catch {
        // ignore malformed frames
      }
    })

    const pingInterval = setInterval(() => {
      if (ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ type: 'PING' }))
      }
    }, 30_000)

    ws.on('close', () => clearInterval(pingInterval))
  })

  priceCache.on('update', () => {
    const frame = JSON.stringify({
      type: 'PRICE_UPDATE',
      prices: priceCache.getAll(),
      timestamp: new Date().toISOString(),
    })
    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) client.send(frame)
    })
  })
}
