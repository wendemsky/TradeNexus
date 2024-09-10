import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ClientPreferences } from 'src/app/models/Client/ClientPreferences';
import { ClientProfile } from 'src/app/models/Client/ClientProfile';
import { ClientPreferencesService } from 'src/app/services/Client/client-preferences.service';
import { ClientProfileService } from 'src/app/services/Client/client-profile.service';

@Component({
  selector: 'app-report-activity',
  templateUrl: './report-activity.component.html',
  styleUrls: ['./report-activity.component.css']
})
export class ReportActivityComponent implements OnInit {

  clientProfileData!: ClientProfile | null //Client Profile data that is set with ClientProfileService

  report_types = [
    {value:"Tax Report",name: "Tax Report"},
    {value:"P&L Report",name: "P&L Report"},
    {value:"Trade Report",name: "Trade Report"},
  ]

  constructor(private clientProfileService: ClientProfileService) {}

  ngOnInit() {
    this.clientProfileService.getClientProfile().subscribe(profile => {
      this.clientProfileData = profile
      console.log('Logged In Client Profile Data: ', this.clientProfileData);
    })
  }

  report: FormGroup = new FormGroup({
    reportType: new FormControl('', Validators.required)
  })

  onSubmit(): void{
    console.log("Report type -> " + JSON.stringify(this.report.value));
  }
}
