import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ClientPortfolio } from '../../shared/models/client.models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class PortfolioService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  getPortfolio(clientId: string): Observable<ClientPortfolio> {
    return this.http.get<ClientPortfolio>(`${this.base}/portfolio/client/${clientId}`);
  }
}
