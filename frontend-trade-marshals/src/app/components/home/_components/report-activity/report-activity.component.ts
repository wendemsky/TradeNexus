import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-report-activity',
  templateUrl: './report-activity.component.html',
  styleUrls: ['./report-activity.component.css']
})
export class ReportActivityComponent {

  report_types = [
    {value:"Tax Report",name: "Tax Report"},
    {value:"P&L Report",name: "P&L Report"},
    {value:"Trade Report",name: "Trade Report"},
  ]

  report: FormGroup = new FormGroup({
    reportType: new FormControl('', Validators.required)
  })

  onSubmit(): void{
    console.log("Report type -> " + JSON.stringify(this.report.value));
  }
}
