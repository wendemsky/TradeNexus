import { Component } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.css']
})
export class RegistrationComponent {

  today = new Date()
  errorMessage: string = ''

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

    if (idType == "Aadhar") {
      return idValue.length != 12 ? { lengthNotMatched: true } : null
    }
    else if (idType == "PAN") {
      return idValue.length != 10 ? { lengthNotMatched: true } : null
    }
    return idValue.length != 10 ? { lengthNotMatched: true } : null
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


  constructor(private route: Router) { }

  //To go back to Landing Page
  redirectToLandingPage() {
    this.route.navigateByUrl('/')
  }

  //Logic to check if the email already exists - For Signup
  validateNewEmail() {
    console.log(this.signupForm.value)
  }

  //Retrieves country entered by user
  get country() {
    return this.personalDetails.get('country')
  }

  //Set Govt ID after submitting personal details form
  setGovtIDType() {
    console.log(this.personalDetails.value)
    this.country?.value === 'India' ? this.identificationTypes = this.indianIds : this.identificationTypes = this.usaIds
  }

  // Submission of Registration Form after getting all details - Save and update our DB
  submitRegistrationForm() {
    console.log(this.identificationDetails.value)
    this.route.navigateByUrl('/home')
  }


}
