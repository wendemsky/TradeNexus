import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, map, Observable, tap, throwError } from 'rxjs';
import { ClientPreferences } from 'src/app/models/Client/ClientPreferences';

@Injectable({
  providedIn: 'root'
})
export class ClientPreferencesService {

  dataURL = 'http://localhost:4000/clients-preferences/';


  constructor(private http: HttpClient) { }

  getClientPreferences(clientId: string | undefined): Observable<ClientPreferences>{
    return this.http.get<ClientPreferences>(this.dataURL)
      .pipe(
        map(clients => clients.find(client => client.clientId === clientId) || null
      ),catchError(this.handleError))
  }

  updateClientPreferences(id:string | undefined, updatedRecord: any){
    return this.http.put(this.dataURL+id, updatedRecord)
      .pipe(catchError(this.handleError))
  }

  setClientPreferences(record: any){
    return this.http.post(this.dataURL, record)
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
      () => 'Unexpected error at service while trying to login user. Please try again later!');
  }
}
