import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-client-preferences',
  templateUrl: './client-preferences.component.html',
  styleUrls: ['./client-preferences.component.css']
})
export class ClientPreferencesComponent {

    toleranceLevel =  [
      {value:1,name: "Very Low"},
      {value:2,name: "Low"},
      {value:3,name: "Moderate"},
      {value:4,name: "High"},
      {value:5,name: "Very High"},
    ]

    purposes = [
      {value:"Retirement",name: "Retirement"},
      {value:"Major Expense",name: "Major Expense"},
      {value:"Education",name: "Education"},
      {value:"Gift",name: "Gift"},
    ]

    incomes = [
      {value: "LIG", name: "Low Income Group (6L and Below)"},
      {value: "MIG", name: "Middle Income Group (Between 6L and 12L)"},
      {value: "HIG", name: "High Income Group (Between 12L and 18L)"},
      {value: "VHIG", name: "Very High Income Group (Above 18L)"},
    ]

    lengths = [
      {value: "Short", name: "Short Term ( <5years )"},
      {value: "Medium", name: "Medium Term ( 5-10 years )"},
      {value: "Long", name: "Long Term ( 10-20 years )"},
    ]

    percentages = [
      {value: "Tier4", name: "Less than 25%"},
      {value: "Tier3", name: "Between 26% and 50%"},
      {value: "Tier2", name: "Between 51% and 75%"},
      {value: "Tier1", name: "More than 75%"},
    ]

    preferences: FormGroup = new FormGroup({
      investmentPurpose: new FormControl('', Validators.required),
      incomeCategory: new FormControl('', Validators.required),
      lengthOfInvestment: new FormControl('', Validators.required),
      percentageOfSpend: new FormControl('', Validators.required),
      riskTolerance: new FormControl(1, Validators.required),
      acceptAdvisor: new FormControl(false, Validators.required)
    })

    ngOnInit(){
     
    }

    onSubmit(){
      console.log(this.preferences.value)
    }

    savePreferences(){
      console.log(this.preferences.value)
    }
}
