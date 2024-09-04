import { inject, Injectable } from '@angular/core';
import { Price } from '../models/price';
import { catchError, Observable, throwError } from 'rxjs';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class PriceService {
  prices: Price[] = [];
  url = 'http://localhost:3000/fmts/trades/prices';

  constructor(private httpClient: HttpClient) { }

  getPrices(): Observable<Price[]> {
    return this.httpClient.get<Price[]>(this.url).pipe(catchError(this.handleError));
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
      () => 'Unexpected error at service while trying to fetch instruments. Please try again later!');
  }

}


