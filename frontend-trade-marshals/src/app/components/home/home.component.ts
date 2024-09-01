import { Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { ClientProfile } from 'src/app/models/Client/ClientProfile';
import { ClientProfileService } from 'src/app/services/Client/client-profile.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent {

  clientProfileData!: ClientProfile | null //Client Profile data that is set with ClientProfileService

  constructor(private dialog: MatDialog, private snackBar: MatSnackBar,
     private clientProfileService: ClientProfileService, private route: Router) { }

  ngOnInit() {
    //Getting Client Profile data from service
    this.clientProfileService.getClientProfile().subscribe(profile => {
      this.clientProfileData = profile
     console.log('Logged In Client Profile Data: ',this.clientProfileData); 
    })
   
   }
}
