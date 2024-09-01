import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, map, Observable, of, tap, throwError } from 'rxjs';

import { Client } from 'src/app/models/Client/Client';

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  dataURL = 'http://localhost:4000/clients';

  constructor(private http: HttpClient) {
  }

  //Validating client email ID with our database - For login and sending back client details
  getValidClientDetails(email: string): Observable<Client | null> {

    return this.http.get<Client[]>(this.dataURL).pipe(
      tap(clients => { console.log(clients) }),
      map(clients =>
        clients.find(client => client.email === email) || null
      ),
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
      () => 'Unexpected error at service while trying to login user. Please try again later!');
  }


}
