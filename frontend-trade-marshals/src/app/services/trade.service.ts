import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Trade } from '../models/trade';
import { Order } from '../models/order';

@Injectable({
  providedIn: 'root'
})
export class TradeService {

  constructor(private httpClient: HttpClient) { }

  orderRequest(order: Order): Observable<Trade> {
    const url = 'http://localhost:3000/fmts/trades/trade';
    return this.httpClient.post<Trade>(url, order);
  }

  addTradeToTradeHistory(trade: Trade) {
     
  }
}
