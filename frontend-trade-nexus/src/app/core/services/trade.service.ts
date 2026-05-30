import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Order, Trade, TradeHistory } from '../../shared/models/trade.models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class TradeService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  executeTrade(order: Order): Observable<Trade> {
    return this.http.post<Trade>(`${this.base}/trade/execute-trade`, order);
  }

  getTradeHistory(clientId: string): Observable<TradeHistory> {
    return this.http.get<TradeHistory>(`${this.base}/trade/trade-history/${clientId}`);
  }
}
