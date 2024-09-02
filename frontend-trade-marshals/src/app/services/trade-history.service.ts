import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Trade } from '../models/trade';
import { mockTrades } from 'src/assets/mock-data/mock-trade-history';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class TradeHistoryService {

  trades: Trade[] = mockTrades;

  constructor(private httpClient: HttpClient) { }

  getTrades(): Observable<Trade[]> {
    return of(this.trades);
    // return this.httpClient.get<Trade[]>('/assets/mock-data/trade-history.json')
  }

  addTrade(trade: Trade) {
    
  }
}
