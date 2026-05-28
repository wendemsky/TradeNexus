import express from 'express'
import cors from 'cors'
import instrumentsRouter from './routes/instruments.js'
import pricesRouter from './routes/prices.js'
import healthRouter from './routes/health.js'
import marketStatusRouter from './routes/marketStatus.js'

const app = express()

app.use(cors())
app.use(express.json())

app.use('/instruments', instrumentsRouter)
app.use('/prices', pricesRouter)
app.use('/health', healthRouter)
app.use('/market-status', marketStatusRouter)

export default app
