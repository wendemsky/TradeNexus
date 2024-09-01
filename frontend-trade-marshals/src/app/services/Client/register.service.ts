import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class RegisterService {

  constructor() { }

  //1. validateNewUser() - Called from component - to check if user new email doesnt exist - from backend; If doesnt exist - success
      //Component then further calls fmts client verif from client-profile
  //2. saveClient() - Called from component - To save client info including clientId from previous fn in our db; If success - component then calls setClientProfile
}
