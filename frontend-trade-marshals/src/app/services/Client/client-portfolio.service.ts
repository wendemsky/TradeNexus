import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, map, Observable, tap, throwError } from 'rxjs';
import { ClientPortfolio } from 'src/app/models/Client/ClientPortfolio';

@Injectable({
  providedIn: 'root'
})
export class ClientPortfolioService {

  private dataURL = 'http://localhost:4000/clients-portfolio';

  constructor(private http: HttpClient) { }

  // To retrieve a particular client's portfolio data by providing client ID
  getClientPortfolio(clientId: string): Observable<any[]>{
    return this.http.get<any[]>(this.dataURL).pipe(
      tap(clientPortfolio => console.log("Client Portfolio -> " + JSON.stringify(clientPortfolio))
      ),
      map( (clientPortfolio) => 
        clientPortfolio
        .filter(portfolio => portfolio.clientId === clientId),
      ),
      catchError(this.handleError)
    );
  }

  updateClientHoldings(url: string, clientPortfolioData: ClientPortfolio): Observable<ClientPortfolio> {
    return this.http.put<ClientPortfolio>(url, clientPortfolioData).pipe(catchError(this.handleError));
  }

  handleError(response: HttpErrorResponse) {
    return throwError(
    () => 'Unable to contact service; please try again later.');
  }

  
}
