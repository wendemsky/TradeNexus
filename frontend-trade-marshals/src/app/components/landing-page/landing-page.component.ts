import { Component, ElementRef, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';

import { LoginService } from 'src/app/services/Client/login.service';

import { Client } from 'src/app/models/Client/Client';
import { ClientProfileService } from 'src/app/services/Client/client-profile.service';
import { ValidatedClient } from 'src/app/models/Client/ValidatedClient';
import { ClientProfile } from 'src/app/models/Client/ClientProfile';

@Component({
  selector: 'app-landing-page',
  templateUrl: './landing-page.component.html',
  styleUrls: ['./landing-page.component.css']
})
export class LandingPageComponent implements OnInit {

  //For Login Form - Popup
  @ViewChild('error') error!: ElementRef;
  @ViewChild('loginFormTemplate') loginFormTemplate!: TemplateRef<any>;

  //Form group for Login Credentials
  loginCredentials: FormGroup = new FormGroup({
    email: new FormControl(''),
    password: new FormControl('')
  })

  clientData!: Client | null  //Logging in Client Data fetched from LoginService
  fmtsValidatedClientData!: ValidatedClient | null //Validated Client data sent after hitting fmts service
  clientProfileData!: ClientProfile | null //Client Profile data that is set with ClientProfileService

  constructor(private dialog: MatDialog, private snackBar: MatSnackBar,
    private loginService: LoginService, private clientProfileService: ClientProfileService, private route: Router) { }

  ngOnInit() { }

  //To Open Popup Form
  openDialog(templateName: string) {
    let dialog: any;
    if (templateName === 'loginFormTemplate')
      dialog = this.dialog.open(this.loginFormTemplate, { width: '50%', height: '62%' });
    else
      console.log('Template doesnt exist');
  }
  //To Close Popup Form
  closeDialog() {
    this.dialog.closeAll()
  }

  //On Submitting Login Form
  onSubmitLoginForm() {
    console.log('Login Form Submitted Details: ',this.loginCredentials.value)

    const snackBarConfig = new MatSnackBarConfig();
    snackBarConfig.duration = 2000;
    snackBarConfig.panelClass = ['form-submit-snackbar'];



    this.loginService.getValidClientDetails(this.loginCredentials.value.email).subscribe(
      {
        next: (data) => { //If success
          this.clientData = data
          console.log('Client data from Service: ',this.clientData)
          if (!this.clientData) { //Client doesnt exist
            this.snackBar.open('User doesnt exist! Enter a valid email', '', snackBarConfig)
            this.loginCredentials.reset()
          }
          else {
            if (this.clientData.password !== this.loginCredentials.value.password) {
              this.snackBar.open('Password doesnt match for given user! Enter a valid password', '', snackBarConfig)
              this.loginCredentials.get('password')?.reset()
            }
            else {
              this.snackBar.open('User has successfully logged in!', '', snackBarConfig)
              //Validate user with fmts
              let fmtsClientData = { 'clientId': this.clientData.clientId, 'email': this.clientData.email }
              this.clientProfileService.fmtsClientVerification(fmtsClientData).subscribe({
                next: (validData) => { //If success
                  this.fmtsValidatedClientData = validData
                  console.log('Fmts validated client data: ', this.fmtsValidatedClientData)
                  //After successful client validation - Set Client Profile data
                  this.clientProfileData = { client: this.clientData, token: this.fmtsValidatedClientData?.token }
                  this.clientProfileService.setClientProfile(this.clientProfileData)
                  //After setting profile redirect to Home Component
                  this.closeDialog()
                  this.redirectToHome()
                },
                error: (e) => { //Error in hitting fmts client data
                  console.log(e)
                  this.snackBar.open(e, '', snackBarConfig)
                  this.loginCredentials.reset()
                }
              })
            }
          }
        },
        error: (e) => { //Error in hitting login service
          console.log(e)
          this.snackBar.open(e, '', snackBarConfig)
          this.loginCredentials.reset()
        }
      }
    )
  }

  //After succesful login
  redirectToHome() {
    this.route.navigateByUrl('/home')
  }

  //On Clicking Registration
  redirectToRegistration() {
    this.route.navigateByUrl('/register')
  }

}
