export interface Order {
  orderId: string;
  instrumentId: string;
  quantity: number;
  targetPrice: number | null;
  direction: 'B' | 'S';
  orderType: 'MARKET' | 'LIMIT';
  clientId: string;
  token: string;
}

export interface Trade {
  tradeId: string;
  order: Order;
  instrumentId: string;
  quantity: number;
  direction: 'B' | 'S';
  clientId: string;
  executionPrice: number;
  cashValue: number;
  executedAt: string;
}

export interface TradeHistory {
  clientId: string;
  trades: Trade[];
}

export interface TradePL {
  instrumentId: string;
  instrumentDescription: string;
  categoryId: string;
  realizedPL: number;
  unrealizedPL: number;
  totalPL: number;
}
