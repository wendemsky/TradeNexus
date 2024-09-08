import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { Client } from 'src/app/models/Client/Client';
import { ClientProfile } from 'src/app/models/Client/ClientProfile';
import { ValidatedClient } from 'src/app/models/Client/ValidatedClient';

import { ClientProfileService } from 'src/app/services/Client/client-profile.service';
import { LoginService } from 'src/app/services/Client/login.service';
import { RegisterService } from 'src/app/services/Client/register.service';

import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';
import { MatStepper } from '@angular/material/stepper';
import { ClientPortfolio } from 'src/app/models/Client/ClientPortfolio';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.css']
})
export class RegistrationComponent implements OnInit {

  today = new Date()
  errorMessage: string = '' //For Form Validation

  //Validate password strength
  passwordStrengthValidator(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      const value = control.value;
      const hasUppercase = /[A-Z]/.test(value);
      const hasLowercase = /[a-z]/.test(value);
      const hasNumber = /\d/.test(value);
      const isValid = hasUppercase && hasLowercase && hasNumber;
      isValid ? this.errorMessage = "" : this.errorMessage = 'Password must contain at least one uppercase letter, one lowercase letter, and one number.';
      return isValid ? null : { passwordStrength: 'Password must contain at least one uppercase letter, one lowercase letter, and one number.' }
    };
  }

  //Validating if password matches
  samePasswordValidator: ValidatorFn = (control: AbstractControl,): ValidationErrors | null => {
    const password = control.get('password');
    const retypePassword = control.get('retypePassword');

    return password && retypePassword && password.value !== retypePassword.value
      ? { identityRevealed: true }
      : null;
  };

  //Validating Govt Proof
  validateGovernmentProof: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
    let idType = control.get('type')?.value;
    let idValue = control.get('value')?.value;

    if (idType === "Aadhar") {
      return idValue?.length !== 12 ? { lengthNotMatched: true } : null
    }
    else if (idType === "PAN") {
      return idValue?.length !== 10 ? { lengthNotMatched: true } : null
    }
    return idValue?.length !== 9 ? { lengthNotMatched: true } : null
  };

  //3 FormGroups for the Registration Step Up Forms
  signupForm: FormGroup = new FormGroup({
    email: new FormControl('', Validators.required),
    password: new FormControl('', [Validators.required, Validators.minLength(8), this.passwordStrengthValidator()]),
    retypePassword: new FormControl('', Validators.required)
  }, { validators: this.samePasswordValidator })

  personalDetails: FormGroup = new FormGroup({
    name: new FormControl('', Validators.required),
    doB: new FormControl('', Validators.required),
    country: new FormControl('', Validators.required),
  })

  identificationDetails: FormGroup = new FormGroup({
    type: new FormControl('', Validators.required),
    value: new FormControl('', Validators.required),
  }, { validators: this.validateGovernmentProof })

  //List of countries and theirs Govt IDs
  countries = [
    { value: "India", name: "India" },
    { value: "USA", name: "USA" },
  ]
  indianIds = [
    { value: "Aadhar", name: "Aadhar Card" },
    { value: "PAN", name: "Permanent Account Number (PAN)" },
  ]
  usaIds = [
    { value: "SSN", name: "Social Security Number (SSN)" },
  ]

  identificationTypes: any //To set the Govt ID Types for the third form

  clientData!: Client | null  //For validation / storing
  fmtsValidatedClientData!: ValidatedClient | null //Validated Client data sent after hitting fmts service
  clientProfileData!: ClientProfile | null //Client Profile data that is set with ClientProfileService
  clientPortfolio!: ClientPortfolio | null //To set Client Portfolio on registering

  constructor(private route: Router, private snackBar: MatSnackBar, private datePipe: DatePipe,
    private loginService: LoginService, private registerService: RegisterService, private clientProfileService: ClientProfileService) { }

  ngOnInit() {
    this.today.setFullYear(this.today.getFullYear() - 18, 11, 31);
    this.fmtsValidatedClientData = null
    this.clientData = null
    this.clientPortfolio = null
    this.signupForm.reset()
    this.personalDetails.reset()
    this.identificationDetails.reset()
  }

  //To go back to Landing Page
  redirectToLandingPage() {
    this.route.navigateByUrl('/')
  }
  //To go back to Client Preferences Page
  redirectToClientPreferencesPage() {
    this.route.navigateByUrl('/home/client-preferences')
  }

  //Logic to check if the email already exists - For Signup
  validateNewEmail(stepper: MatStepper) {
    console.log('Sign Up Form Submitted Details: ', this.signupForm.value)

    const snackBarConfig = new MatSnackBarConfig();
    snackBarConfig.duration = 3000;
    snackBarConfig.panelClass = ['form-submit-snackbar'];

    this.loginService.getValidClientDetails(this.signupForm.value.email).subscribe(
      {
        next: (data) => { //If success
          this.clientData = data
          console.log('Client data from Service: ', this.clientData)
          if (this.clientData) { //Registering an existing Client 
            this.snackBar.open('User is already registered! Enter a valid email', '', snackBarConfig)
            this.signupForm.reset()
          }
          else { //Client can be registered - Passing to FMTS
            let fmtsClientData = { 'clientId': '', 'email': this.signupForm.value.email }
            this.clientProfileService.fmtsClientVerification(fmtsClientData).subscribe({
              next: (validData) => { //If success
                this.fmtsValidatedClientData = validData
                console.log('Fmts validated client data: ', this.fmtsValidatedClientData)
                //Client is now successfully registered and clientId is retrieved - Going to next stop
                stepper.next()
              },
              error: (e) => { //Error in hitting FMTS service - Cannot register client
                console.log(e)
                this.snackBar.open(String(e), '', snackBarConfig)
                this.signupForm.reset()
              }
            })
          }
        },
        error: (e) => { //Error in hitting login service
          console.log(e)
          this.snackBar.open(e, '', snackBarConfig)
          this.signupForm.reset()
        }
      })
  }

  //Retrieves country entered by user
  get country() {
    return this.personalDetails.get('country')
  }

  //Set Govt ID after submitting personal details form
  setGovtIDType(stepper: MatStepper) {
    console.log(this.personalDetails.value)
    this.country?.value === 'India' ? this.identificationTypes = this.indianIds : this.identificationTypes = this.usaIds
    stepper.next() //Manually go to next step
  }

  // Submission of Registration Form after getting all details - Save and update our DB
  submitRegistrationForm() {
    console.log('Govt ID Details Submitted: ', this.identificationDetails.value)

    const snackBarConfig = new MatSnackBarConfig();
    snackBarConfig.duration = 3000;
    snackBarConfig.panelClass = ['form-submit-snackbar'];


    //To verify if the client has entered unique govt id
    this.registerService.checkUniqueGovtIDDetails({ 'type': this.identificationDetails.value.type, 'value': this.identificationDetails.value.value }).subscribe({
      next: (data) => {
        if (data) { //Which means client doesnt have unique govt id
          this.snackBar.open("Entered Govt ID details aren't unique! Couldn't register client!", '', snackBarConfig)
          this.identificationDetails.reset()
        }
        else {
          //To get the data that is to be saved : clientData - Every client registering from platform is not an admin

          let formattedDate = this.datePipe.transform(this.personalDetails.value?.doB, 'MM/dd/yyyy');  //Transform doB with pipe operator
          this.clientData = {
            'email': this.signupForm.value.email,
            'clientId': String(this.fmtsValidatedClientData?.clientId),
            'password': this.signupForm.value.password,
            'name': this.personalDetails.value.name,
            'dateOfBirth': String(formattedDate),
            'country': this.personalDetails.value.country,
            'identification': [{ 'type': this.identificationDetails.value.type, 'value': this.identificationDetails.value.value }],
            'isAdmin': false
          }
          console.log('Registered Data to be posted to the Service: ', this.clientData)
          //Saving Registered client with service
          this.registerService.saveClientDetails(this.clientData).subscribe({
            next: (data) => {
              if (data && data.clientId === this.clientData?.clientId) { //Successfully saved registered client details
                //Save client Portfolio details
                this.clientPortfolio = { 'clientId': String(data.clientId), 'currBalance': 1000000, 'holdings': [] }
                this.registerService.saveClientPortfolioDetails(this.clientPortfolio).subscribe({
                  next: (data) => {
                    if (data && data.clientId === this.clientPortfolio?.clientId) { //Successfully created client portfolio too
                      this.snackBar.open('Client has been registered and portfolio has been created with $1000000 (1 mil) balance!', '', snackBarConfig)
                    }
                    else {
                      this.snackBar.open('Client has been registered but and portfolio couldnt be created! Unexpected error at service!', '', snackBarConfig)
                    }
                  },
                  error: (e) => {
                    console.log('Portfolio creation error: ', e)
                    this.snackBar.open('Client has been registered but and portfolio couldnt be created! Unexpected error at service!', '', snackBarConfig)
                  }
                })
                //After successful client validation - Set Client Profile data
                this.clientProfileData = { client: this.clientData, token: this.fmtsValidatedClientData?.token }
                this.clientProfileService.setClientProfile(this.clientProfileData)
                //After setting profile redirect to Client Preferences Component
                this.redirectToClientPreferencesPage()
              }
              else {
                this.snackBar.open('Client couldnt be registered! Please try again later!', '', snackBarConfig)
              }
            },
            error: (e) => {
              console.log('Registering Client error: ', e)
              this.snackBar.open(e, '', snackBarConfig)
            }
          })
        }
      },
      error: (e) => {
        console.log('Registering Client error: ', e)
        this.snackBar.open(String(e), '', snackBarConfig)
      }
    })

  }

}
