import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, catchError, Observable, of, tap, throwError } from 'rxjs';
import { ClientProfile } from 'src/app/models/Client/ClientProfile';
import { ValidatedClient } from 'src/app/models/Client/ValidatedClient';

@Injectable({
  providedIn: 'root'
})
export class ClientProfileService {

  fmtsURL = 'http://localhost:3000/fmts/client';

  // private clientProfileSubject = new BehaviorSubject<ClientProfile | null>(null);
  // clientProfile$ = this.clientProfileSubject.asObservable();

  constructor(private http: HttpClient) { }

  //To verify a client after successful login/register before redirecting the client
  fmtsClientVerification(clientData: any): Observable<ValidatedClient | null> {
    //If login - clientData has clientId
    //If signup - ClientData has no clientId
    return this.http.post<any>(this.fmtsURL, clientData).pipe(
      tap(clientValidData => { console.log('Validated Client Data: ', clientValidData) }),
      catchError(this.handleFMTSError)
    )
  }

  //Getting Client Profile Details - Called from every other component after successful login/register
  getClientProfile(): Observable<any> {
    let profile = localStorage.getItem('clientDetails')!
    return of(JSON.parse(profile))
  }

  //Setting Client Profile Details - Called from Landing Page/Register component after successful login/register
  setClientProfile(profile: any) {
    localStorage.setItem('clientDetails', JSON.stringify(profile))
  }

  removeClientProfile() {
    localStorage.removeItem('clientDetails')
  }

  //Function to handle FMTS endpoint errors
  handleFMTSError(response: HttpErrorResponse) {
    if (response.error instanceof ProgressEvent) {
      console.error('There is a client-side or network error - ' +
        `${response.message} ${response.status} ${response.statusText}`);
      return throwError(
        () => 'Unexpected error at service while validating client data. Please try again later!');
    }
    else if (response.status === 406) { //ClientId and email dont match - for login
      console.error(`The client details couldnt be validated: ${response.status}, ` +
        `and body: ${JSON.stringify(response.error)}`);
      return throwError(
        () => 'Client details couldnt be validated! Please try again later!');
    }
    console.error(`Error response with status: ${response.status}, ` +
      `and body: ${JSON.stringify(response.error)}`);
    return throwError( //For status codes 404 etc
      () => 'Unexpected error at service while validating client data. Please try again later!');
  }

}