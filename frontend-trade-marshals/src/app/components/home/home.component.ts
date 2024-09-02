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
  isSideMenuExpanded: boolean = false; //Side Menu
  isHomeContent:boolean = true; //Home Content displayed

  constructor(private dialog: MatDialog, private snackBar: MatSnackBar,
    private clientProfileService: ClientProfileService, private route: Router) { }

  ngOnInit() {
    this.isSideMenuExpanded = false
    if(this.route.url === '/home')
      this.isHomeContent = true //Set Home Page Content
    else
      this.isHomeContent = false
    //Getting Client Profile data from service
    this.clientProfileService.getClientProfile().subscribe(profile => {
      this.clientProfileData = profile
      console.log('Logged In Client Profile Data: ', this.clientProfileData);
    })
  }

  //To go to Home
  redirectToHome() {
    this.isHomeContent = true
    this.route.navigateByUrl('/home')
  }

  //To go to Profile Component
  redirectToProfile() {
    this.isHomeContent = false
    this.route.navigateByUrl('/home/profile')
  }

  //When client logs out - To go back to Landing
  logout() {
    this.clientProfileService.removeClientProfile() //Erasing Client data
    this.route.navigateByUrl('/')
  }
}
