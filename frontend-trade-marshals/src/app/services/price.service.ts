import { Injectable } from '@angular/core';
import { Price } from '../models/price';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class PriceService {
  prices: Price[] = [];
  url = 'http://localhost:3000/fmts/trades/prices';


  constructor(private httpClient: HttpClient) { }

  getPrices(): Observable<Price[]> {
    return this.httpClient.get<Price[]>(this.url);
  }

}
