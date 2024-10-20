import { Injectable } from '@angular/core';
import { BehaviorSubject, catchError, Observable, of, tap, throwError } from 'rxjs';
import { ClientProfile } from 'src/app/models/Client/ClientProfile';

@Injectable({
  providedIn: 'root'
})
export class ClientProfileService {

  // private clientProfileSubject = new BehaviorSubject<ClientProfile | null>(null);
  // clientProfile$ = this.clientProfileSubject.asObservable();

  constructor() { }

  //Getting Client Profile Details - Called from every other component after successful login/register
  getClientProfile(): Observable<ClientProfile> {
    let profile = localStorage.getItem('clientDetails')!
    return of(JSON.parse(profile))
  }

  //Setting Client Profile Details - Called from Landing Page/Register component after successful login/register
  setClientProfile(profile:ClientProfile) {
    localStorage.setItem('clientDetails', JSON.stringify(profile) )
  }

  //Removing Client Profile from local storage on Logout
  removeClientProfile(){
    localStorage.removeItem('clientDetails')
  }

}