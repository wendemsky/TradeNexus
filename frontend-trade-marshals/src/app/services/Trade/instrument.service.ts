import { inject, Injectable } from '@angular/core';
import { Instrument } from '../../models/Trade/instrument';
import { catchError, Observable, of, throwError } from 'rxjs';
// import { MockInstruments } from '../../assets/mock-data/mock-instruments';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class InstrumentService {
  //instruments: Instrument[] = MockInstruments;
  private _snackBar = inject(MatSnackBar);
  url: string = 'http://localhost:3000/fmts/trades/instruments'

  constructor(
    private httpClient: HttpClient
  ) { }

  getInstruments(): Observable<Instrument[] | null> {
    return this.httpClient.get<Instrument[]>(this.url).pipe(catchError(this.handleError));
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
