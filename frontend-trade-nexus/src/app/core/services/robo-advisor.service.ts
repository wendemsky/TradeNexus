import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ClientPreferences, Holding } from '../../shared/models/client.models';
import { Price } from '../../shared/models/price.models';
import { environment } from '../../../environments/environment';

interface SellRequest {
  clientId: string;
  preferences: ClientPreferences;
}

@Injectable({ providedIn: 'root' })
export class RoboAdvisorService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  suggestBuy(preferences: ClientPreferences): Observable<Price[]> {
    return this.http.post<Price[]>(`${this.base}/trade/suggest-buy`, preferences);
  }

  suggestSell(body: SellRequest): Observable<Holding[]> {
    return this.http.post<Holding[]>(`${this.base}/trade/suggest-sell`, body);
  }
}
