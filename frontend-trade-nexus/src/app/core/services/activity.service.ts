import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Holding, ClientPreferences } from '../../shared/models/client.models';
import { TradeHistory, TradePL } from '../../shared/models/trade.models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ActivityService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  getHoldings(clientId: string): Observable<Holding[]> {
    return this.http.get<Holding[]>(`${this.base}/activity-report/holdings/${clientId}`);
  }

  getTrades(clientId: string): Observable<TradeHistory> {
    return this.http.get<TradeHistory>(`${this.base}/activity-report/trades/${clientId}`);
  }

  getPL(clientId: string): Observable<TradePL[]> {
    return this.http.get<TradePL[]>(`${this.base}/activity-report/pl/${clientId}`);
  }
}
