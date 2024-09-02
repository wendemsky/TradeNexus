import { Injectable } from '@angular/core';
import { catchError, map, Observable, of, tap } from 'rxjs';
import { Trade } from '../models/trade';
import { mockTrades } from 'src/assets/mock-data/mock-trade-history';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class TradeHistoryService {

  // trades: Trade[] = mockTrades;

  constructor(private httpClient: HttpClient) { }

  getTrades(clientId: string): Observable<Trade[]> {
    // return of(this.trades);
    return this.httpClient.get<Trade[]>('http://localhost:4000/trades').pipe(
      tap(trades => { console.log(trades)}),
      map(trades => 
        trades.filter(trade => trade.clientId === clientId),
      )
    )
  }
}
