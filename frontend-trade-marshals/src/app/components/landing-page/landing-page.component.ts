import { Component, ElementRef, OnInit, TemplateRef, ViewChild } from '@angular/core';

import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';

import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { LoginService } from 'src/app/services/Client/login.service';

@Component({
  selector: 'app-landing-page',
  templateUrl: './landing-page.component.html',
  styleUrls: ['./landing-page.component.css']
})
export class LandingPageComponent implements OnInit {

  //For Login Form - Popup
  @ViewChild('error') error!: ElementRef;
  @ViewChild('loginFormTemplate') loginFormTemplate!: TemplateRef<any>;

  loginCredentials: FormGroup = new FormGroup({
    email: new FormControl(''),
    password: new FormControl('')
  })

  constructor(private dialog: MatDialog, private snackBar: MatSnackBar, 
    private loginService: LoginService, private route: Router) { }
  
  ngOnInit() { }

  //To Open Popup Form
  openDialog(templateName: string) {
    let dialog: any;
    if (templateName === 'loginFormTemplate')
      dialog = this.dialog.open(this.loginFormTemplate, { width: '50%', height: '60%' });
    else
      console.log('Template doesnt exist');
  }
  //To Close Popup Form
  closeDialog() {
    this.dialog.closeAll()
  }

  //On Submitting Login Form
  onSubmitLoginForm() {
    console.log(this.loginCredentials.value)
    console.log(this.loginService.validateEmail(this.loginCredentials.value.email, this.loginCredentials.value.password))
  }
  
  redirectToRegistration(){
    this.route.navigateByUrl('/register')
  }

}
