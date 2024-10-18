import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, map, Observable, tap, throwError } from 'rxjs';
import { ClientPortfolio } from 'src/app/models/Client/ClientPortfolio';

@Injectable({
  providedIn: 'root'
})
export class ClientPortfolioService {

  private dataURL = 'http://localhost:8080/portfolio/client/';

  constructor(private http: HttpClient) { }

  // To retrieve a particular client's portfolio data by providing client ID
  getClientPortfolio(clientId: string): Observable<ClientPortfolio>{
    return this.http.get<ClientPortfolio>(this.dataURL + clientId)
  }

  updateClientHoldings(url: string, clientPortfolioData: ClientPortfolio): Observable<ClientPortfolio> {
    return this.http.put<ClientPortfolio>(url, clientPortfolioData).pipe(catchError(this.handleError));
  }

  handleError(response: HttpErrorResponse) {
    return throwError(
    () => 'Unable to contact service; please try again later.');
  }

  
}
