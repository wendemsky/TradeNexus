import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ClientProfile } from 'src/app/models/Client/ClientProfile';
import { ClientProfileService } from 'src/app/services/Client/client-profile.service';

@Component({
  selector: 'app-report-activity',
  templateUrl: './report-activity.component.html',
  styleUrls: ['./report-activity.component.css']
})
export class ReportActivityComponent implements OnInit {

  clientProfileData!: ClientProfile | null //Client Profile data that is set with ClientProfileService

  report_types = [
    {value:"Holdings Report",name: "Holdings Report"}, //Get clients holdings
    {value:"P&L Report",name: "P&L Report"}, //Get Clients P&L
    {value:"Trade Report",name: "Trade Report"}, //Get Clients trading history
  ]

  //For now hardcoding clients - But should retrieve from service
  clients = [
    {value:'1654658069', name:'Sowmya'},
    {value: '541107416', name: 'Himanshu'},
    {value: '1425922638', name: 'Rishiyanth'},
    {value: '1644724236', name: 'Aditi'},
    {value: '1236679496', name: 'Mohith'}
  ]

  constructor(private clientProfileService: ClientProfileService) {}

  ngOnInit() {
    this.clientProfileService.getClientProfile().subscribe(profile => {
      this.clientProfileData = profile
      console.log('Logged In Client Profile Data: ', this.clientProfileData);
    })
  }

  //Form Groups for logged in client's report
  yourReport: FormGroup = new FormGroup({
    reportType: new FormControl('', Validators.required)
  })

  //Form Groups for other client's report
  othersReport: FormGroup = new FormGroup({
    clientId: new FormControl('', Validators.required),
    reportType: new FormControl('', Validators.required)
  })

  //On Submitting logged in clients report
  onSubmitYourReportForm() {
    console.log("Your Report type -> " + JSON.stringify(this.yourReport.value));
  }

  //On Submitting other clients report
  onSubmitOthersReportForm() {
    console.log(`Report type of Client ${this.othersReport.value.clientID} is ${this.othersReport.value.reportType}`);
  }
}
