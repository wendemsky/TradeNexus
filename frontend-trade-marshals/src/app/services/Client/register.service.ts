import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, map, Observable, of, tap, throwError } from 'rxjs';

import { Client } from 'src/app/models/Client/Client';
import { ClientPortfolio } from 'src/app/models/Client/ClientPortfolio';

@Injectable({
  providedIn: 'root'
})
export class RegisterService {

  clientDataURL = 'http://localhost:4000/clients';
  clientPortfolioDataURL = 'http://localhost:4000/clients-portfolio'

  constructor(private http: HttpClient) { }

  //Validating new client's govt ID details to be unique - Before registering client 
  checkUniqueGovtIDDetails(govtID: any): Observable<Client | null> {
    return this.http.get<Client[]>(this.clientDataURL).pipe(
      tap(clients => { console.log(clients) }),
      map(clients =>
        clients.find(client =>
          client.identification.some((id) =>
            id.type === govtID.type && id.value === govtID.value
          )) || null
      ),
      catchError(this.handleError)
    )
  }

  //Saving Registered Client Details in our database
  saveClientDetails(clientData: Client): Observable<Client | null> {
    return this.http.post<Client>(this.clientDataURL, clientData).pipe(
      tap(client => { console.log(client) }),
      catchError(this.handleError)
    )
  }

  //Saving Registered Client Details in our database
  saveClientPortfolioDetails(clientPortfolioData: ClientPortfolio): Observable<ClientPortfolio | null> {
    return this.http.post<ClientPortfolio>(this.clientPortfolioDataURL, clientPortfolioData).pipe(
      tap(client => { console.log(client) }),
      catchError(this.handleError)
    )
  }

  //Function to handle errors
  handleError(response: HttpErrorResponse) {

    if (response.error instanceof ProgressEvent) {
      console.error('There is a client-side or network error - ' +
        `${response.message} ${response.status} ${response.statusText}`);
    } else {
      console.error(`There is an error with status: ${response.status}, ` +
        `and body: ${JSON.stringify(response.error)}`);
    }
    return throwError(
      () => 'Unexpected error at service while trying to register user. Please try again later!');
  }

}
