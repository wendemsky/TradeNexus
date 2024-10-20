import { inject, Injectable } from '@angular/core';
import { catchError, map, Observable, of, tap, throwError } from 'rxjs';
import { Trade } from '../../models/Trade/trade';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Order } from '../../models/Trade/order';

@Injectable({
  providedIn: 'root'
})
export class TradeHistoryService {

  // trades: Trade[] = mockTrades;
  private _snackBar = inject(MatSnackBar);
  url = 'http://localhost:4000/trades';

  constructor(private httpClient: HttpClient) { }

  getTrades(clientId: string): Observable<Trade[]> {
    // return of(this.trades);
    return this.httpClient.get<Trade[]>(this.url).pipe(
      tap(trades => { console.log(trades)}),
      map(trades => 
        trades.filter(trade => trade.clientId === clientId),
      ),
      catchError(this.handleError)
    )
  }

  addTrade(trade: Trade) {
    return this.httpClient.post<Trade>(this.url, trade)
    .pipe(catchError(this.handleError))
  }

  handleError(response: HttpErrorResponse) {
    if (response.error instanceof ProgressEvent) {
      console.error('There is a client-side or network error - ' +
        `${response.message} ${response.status} ${response.statusText}`);
    } else {
      console.error(`There is an error with status: ${response.status}, ` +
        `and body: ${JSON.stringify(response.error)}`);
    }
    
    return throwError(
      () => 'Unexpected error at service while trying to fetch trade history. Please try again later!');
  }
}

