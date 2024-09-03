import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { catchError, Observable, throwError } from 'rxjs';
import { Trade } from '../models/trade';
import { Order } from '../models/order';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class TradeService {
  private _snackBar = inject(MatSnackBar);

  constructor(private httpClient: HttpClient) { }

  orderRequest(order: Order): Observable<Trade> {
    const url = 'http://localhost:3000/fmts/trades/trade';
    return this.httpClient.post<Trade>(url, order).pipe(catchError(this.handleError));
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
      () => 'Unexpected error at service while trying to save trade. Please try again later!');
  }
}

