import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { ClientPreferences } from 'src/app/models/Client/ClientPreferences';
import { ClientProfile } from 'src/app/models/Client/ClientProfile';
import { ClientPreferencesService } from 'src/app/services/Client/client-preferences.service';
import { ClientProfileService } from 'src/app/services/Client/client-profile.service';

@Component({
  selector: 'app-client-preferences',
  templateUrl: './client-preferences.component.html',
  styleUrls: ['./client-preferences.component.css']
})
export class ClientPreferencesComponent {

  clientProfileData!: ClientProfile
  clientPreferencesData!: any
  isClientFormFilled: boolean = false

  toleranceLevel = [
    { value: 1, name: "Very Low" },
    { value: 2, name: "Low" },
    { value: 3, name: "Moderate" },
    { value: 4, name: "High" },
    { value: 5, name: "Very High" },
  ]

  purposes = [
    { value: "Retirement", name: "Retirement" },
    { value: "Major Expense", name: "Major Expense" },
    { value: "Education", name: "Education" },
    { value: "Gift", name: "Gift" },
  ]

  incomes = [
    { value: "LIG", name: "Low Income Group (6L and Below)" },
    { value: "MIG", name: "Middle Income Group (Between 6L and 12L)" },
    { value: "HIG", name: "High Income Group (Between 12L and 18L)" },
    { value: "VHIG", name: "Very High Income Group (Above 18L)" },
  ]

  lengths = [
    { value: "Short", name: "Short Term ( <5years )" },
    { value: "Medium", name: "Medium Term ( 5-10 years )" },
    { value: "Long", name: "Long Term ( 10-20 years )" },
  ]

  percentages = [
    { value: "Tier4", name: "Less than 25%" },
    { value: "Tier3", name: "Between 26% and 50%" },
    { value: "Tier2", name: "Between 51% and 75%" },
    { value: "Tier1", name: "More than 75%" },
  ]

  preferences: FormGroup = new FormGroup({
    investmentPurpose: new FormControl('', Validators.required),
    incomeCategory: new FormControl('', Validators.required),
    lengthOfInvestment: new FormControl('', Validators.required),
    percentageOfSpend: new FormControl('', Validators.required),
    riskTolerance: new FormControl(1),
    acceptAdvisor: new FormControl(false)
  })

  constructor(private clientPreferencesService: ClientPreferencesService, private clientProfileService: ClientProfileService,
    private router: Router, private route: ActivatedRoute, private snackBar: MatSnackBar) { }

  ngOnInit() {
    this.clientProfileService.getClientProfile().subscribe(profile => {
      this.clientProfileData = profile
      console.log('Logged In Client Profile Data: ', this.clientProfileData);
      this.clientPreferencesService.getClientPreferences(this.clientProfileData?.client?.clientId).subscribe({
        next: (data: any) => {
          if (data) {
            this.isClientFormFilled = true
            this.clientPreferencesData = data
            this.setPreferencesFormData(this.clientPreferencesData)
          }
          else {
            this.clientPreferencesData = null
          }
        },
        error: (err) => {
          console.log(err)

        }
      })
    })
  }

  setPreferencesFormData(clientPreferences: any) {
    this.preferences.patchValue({
      investmentPurpose: clientPreferences.investmentPurpose,
      incomeCategory: clientPreferences.incomeCategory,
      lengthOfInvestment: clientPreferences.lengthOfInvestment,
      percentageOfSpend: clientPreferences.percentageOfSpend,
      riskTolerance: clientPreferences.riskTolerance,
      acceptAdvisor: clientPreferences.acceptAdvisor,
    })
  }

  // onSubmit() {

  //   const snackBarConfig = new MatSnackBarConfig();
  //   snackBarConfig.duration = 3000;
  //   snackBarConfig.panelClass = ['form-submit-snackbar'];

  //   let obj = this.preferences.getRawValue()
  //   obj.clientId = this.clientProfileData?.client?.clientId
  //   this.clientPreferencesService.setClientPreferences(obj).subscribe({
  //     next: () => {
  //       this.snackBar.open('Preferences submitted successfully', '', snackBarConfig)
  //     },
  //     error: (err) => {
  //       this.snackBar.open(err, '', snackBarConfig)
  //     }
  //   })
  // }

  savePreferences() {

    const snackBarConfig = new MatSnackBarConfig();
    snackBarConfig.duration = 3000;
    snackBarConfig.panelClass = ['form-submit-snackbar'];

    let obj = this.preferences.getRawValue()
    obj.clientId = this.clientProfileData?.client?.clientId
    if (this.isClientFormFilled) {
      this.clientPreferencesService.updateClientPreferences(this.clientPreferencesData?.id, obj).subscribe({
        next: (data: any) => {
          console.log('New Preferences Submmited Data: ', data)
          if (data && data.clientId === this.clientProfileData?.client?.clientId) {
            this.snackBar.open('Preferences updated successfully', '', snackBarConfig)
            this.redirectToHome()
          }
          else {
            this.snackBar.open('Client preferences couldnt be updated! Unexpected error at service!', '', snackBarConfig)
          }
        },
        error: (err) => {
          this.snackBar.open(err, '', snackBarConfig)
        }
      })
    }
    else {
      this.clientPreferencesService.setClientPreferences(obj).subscribe({
        next: (data: any) => {
          console.log('New Preferences Submmited Data: ', data)
          if (data && data.clientId === this.clientProfileData?.client?.clientId) {
            this.snackBar.open('Preferences saved successfully', '', snackBarConfig)
            this.isClientFormFilled = true
            this.clientPreferencesData = data
            this.redirectToHome()
          }
          else {
            this.snackBar.open('Client preferences couldnt be saved! Unexpected error at service!', '', snackBarConfig)
          }

        },
        error: (err) => {
          this.snackBar.open(err, '', snackBarConfig)
        }
      })
    }
  }

  redirectToHome() {
    this.router.navigate(['../'], { relativeTo: this.route });
    
 
  }
}
