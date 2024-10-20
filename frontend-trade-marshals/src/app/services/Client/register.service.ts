import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, map, Observable, of, tap, throwError } from 'rxjs';

import { Client } from 'src/app/models/Client/Client';
import { ClientPortfolio } from 'src/app/models/Client/ClientPortfolio';
import { ClientProfile } from 'src/app/models/Client/ClientProfile';
import { IsVerifiedClient } from 'src/app/models/Client/IsVerifiedClient';

@Injectable({
  providedIn: 'root'
})
export class RegisterService {

  clientDataURL = 'http://localhost:8080/client/register';
  url = "http://localhost:8080/client/verify-email/"


  constructor(private http: HttpClient) { }

  //Validating client email ID with our database - For login and sending back client details
  getVerificationOfClientEmail(email: string): Observable<IsVerifiedClient> {
    return this.http.get<IsVerifiedClient>(this.url + email).pipe(
      tap(data => { console.log(data) }),
      catchError(this.handleError)
    )
  }

  //Saving Registered Client Details in our database
  saveClientDetails(clientData: Client): Observable<ClientProfile | null> {
    return this.http.post<ClientProfile>(this.clientDataURL, clientData).pipe(
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
    if(response.status == 500 || response.status==0 ){
      return throwError(
        () => 'Unexpected error at service while trying to register user. Please try again later!'
      );
    }
    return throwError(
      () => response.error.message
    );
  }

}
