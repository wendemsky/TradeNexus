import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';

import { Client } from 'src/app/models/Client/Client';

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  dataURL = '../../../assets/mock-data/client.json';

  constructor(private http: HttpClient) {
  }

  //Validating client email ID with our database - For login
  validateEmail(email: string, password: string): boolean[] {
    let isUserExists: boolean = false
    let isPasswordMatches: boolean = false

    this.http.get<Client[]>(this.dataURL)
      .subscribe((clients: any) => {
        console.log(clients)
        clients.map((client: any) => {
          if (client.email === email) {
            isUserExists = true //Client's login email is valid
            if (client.password === password)
              isPasswordMatches = true //Client's login password matches
          }
        })
      })
    return [isUserExists, isPasswordMatches]
  }



  //Returning client information for given mail (For login only)

  //FMTS validation for mail and client
}
