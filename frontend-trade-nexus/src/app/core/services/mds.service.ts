import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Price } from '../../shared/models/price.models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class MdsService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.mdsHttpUrl;

  getAllPrices(): Observable<Price[]> {
    return this.http.get<Price[]>(`${this.base}/prices`);
  }

  getPriceHistory(instrumentId: string): Observable<Price[]> {
    return this.http.get<Price[]>(`${this.base}/prices/${instrumentId}/history`);
  }
}
