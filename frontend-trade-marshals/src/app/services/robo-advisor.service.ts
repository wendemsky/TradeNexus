import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ClientPreferencesService } from './Client/client-preferences.service';
import { ClientProfileService } from './Client/client-profile.service';
import { catchError, Observable, throwError } from 'rxjs';
import { Price } from '../models/price';
import { ClientPreferences } from '../models/Client/ClientPreferences';

@Injectable({
  providedIn: 'root'
})
export class RoboAdvisorService {

  url = 'http://localhost:8080/trade/'

  constructor(private httpClient: HttpClient) { }
  
  getTopBuyTrades(clientPreferences: ClientPreferences): Observable<Price[]>{
      const headers = new HttpHeaders({
        'Content-Type': 'application/json'
      });
      let apiUrl = this.url + 'suggest-buy';
      return this.httpClient.post<Price[]>(apiUrl, clientPreferences, {headers}).pipe(catchError(this.handleError));
  }

  getTopSellTrades(clientPreferences: ClientPreferences): Observable<Price[]>{
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });
    let apiUrl = this.url + 'suggest-sell';
    return this.httpClient.post<Price[]>(apiUrl, clientPreferences, {headers}).pipe(catchError(this.handleError));
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
