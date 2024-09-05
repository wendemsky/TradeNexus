import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegistrationComponent } from './registration.component';
import { DatePipe } from '@angular/common';
import { MaterialModule } from 'src/app/material.module';
import { of, throwError } from 'rxjs';
import { LoginService } from 'src/app/services/Client/login.service';
import { RegisterService } from 'src/app/services/Client/register.service';
import { ClientProfileService } from 'src/app/services/Client/client-profile.service';
import { ReactiveFormsModule } from '@angular/forms';
import { provideAnimations } from '@angular/platform-browser/animations';
import { Client } from 'src/app/models/Client/Client';
import { ValidatedClient } from 'src/app/models/Client/ValidatedClient';
import { ClientProfile } from 'src/app/models/Client/ClientProfile';
import { Router } from '@angular/router';
import { ClientPortfolio } from 'src/app/models/Client/ClientPortfolio';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';

const testExistingClient: Client = {
  "email": "existing.user@gmail.com",
  "clientId": "5411274193",
  "password": "password",
  "name": "Existing User",
  "dateOfBirth": "08/12/2002",
  "country": "India",
  "identification": [
    {
      "type": "Aadhar",
      "value": "123412341234"
    }
  ],
  "isAdmin": true
}

const testClient: Client = {
  "email": "sample.user@gmail.com",
  "clientId": "5411274199",
  "password": "password",
  "name": "Sample User",
  "dateOfBirth": "08/12/2002",
  "country": "India",
  "identification": [
    {
      "type": "Aadhar",
      "value": "123412341234"
    }
  ],
  "isAdmin": true
}

const validatedClient: ValidatedClient = {
  "email": "sample.user@gmail.com",
  "clientId": "5411274199",
  "token": 1252630773
}

const testClientProfile: ClientProfile =
{
  "client":
  {
    "email": "sample.user@gmail.com",
    "clientId": "5411274199",
    "password": "password",
    "name": "Sample User",
    "dateOfBirth": "08/12/2002",
    "country": "India",
    "identification": [
      {
        "type": "Aadhar",
        "value": "123412341234"
      }
    ],
    "isAdmin": true
  },
  "token": 1252630773
}

const testClientPortfolio: ClientPortfolio = {
  "clientId": "5411274199",
  "currBalance": 10000000,
  "holdings": []
}

describe('RegistrationComponent', () => {
  let component: RegistrationComponent;
  let fixture: ComponentFixture<RegistrationComponent>;
  let datePipe: DatePipe
  let router: Router;

  let loginMockService: any
  let mockGetClientSpy: any
  let clientProfileMockService: any
  let mockValidatedClientSpy: any
  let mockClientProfileSetSpy: any
  let registerMockService: any
  let mockUniqueGovtIDSpy: any
  let mockAddClientSpy: any
  let mockAddClientPortfolioSpy: any

  let snackBar: any
  let snackBarSpy: any

  beforeEach(async () => {

    loginMockService = jasmine.createSpyObj('LoginService', ['getValidClientDetails']);
    mockGetClientSpy = loginMockService.getValidClientDetails.and.returnValue(of(testExistingClient));

    clientProfileMockService = jasmine.createSpyObj('ClientProfileService', ['fmtsClientVerification', 'setClientProfile']);
    mockValidatedClientSpy = clientProfileMockService.fmtsClientVerification.and.returnValue(of(validatedClient));
    mockClientProfileSetSpy = clientProfileMockService.setClientProfile.and.callFake((param: any) => { return of(param) })

    registerMockService = jasmine.createSpyObj('RegisterService', ['checkUniqueGovtIDDetails', 'saveClientDetails', 'saveClientPortfolioDetails']);
    mockUniqueGovtIDSpy = registerMockService.checkUniqueGovtIDDetails.and.returnValue(of(null))
    mockAddClientSpy = registerMockService.saveClientDetails.and.callFake((param: any) => { return of(param) })
    mockAddClientPortfolioSpy = registerMockService.saveClientPortfolioDetails.and.callFake((param: any) => { return of(param) })

    snackBar = jasmine.createSpyObj('MatSnackBar', ['open']); //Spying on MatSnackBar

    await TestBed.configureTestingModule({
      declarations: [RegistrationComponent,],
      imports: [
        MaterialModule,
        ReactiveFormsModule
      ],
      providers: [
        DatePipe,
        { provide: LoginService, useValue: loginMockService },
        { provide: RegisterService, useValue: registerMockService },
        { provide: ClientProfileService, useValue: clientProfileMockService },
        { provide: Router, useClass: class { navigateByUrl = jasmine.createSpy('navigateByUrl'); } },
        { provide: MatSnackBar, useValue: snackBar },
        provideAnimations()
      ]
    })
      .compileComponents();

    datePipe = TestBed.inject(DatePipe)
    fixture = TestBed.createComponent(RegistrationComponent);
    component = fixture.componentInstance;
    snackBarSpy = TestBed.inject(MatSnackBar) as jasmine.SpyObj<MatSnackBar>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  /*REGISTRATION FORM VALIDATION*/
  it('should invalidate password if it lacks strength', () => {
    const passwordControl = component.signupForm.get('password');
    passwordControl?.setValue('PASS1');
    passwordControl?.updateValueAndValidity();
    expect(passwordControl?.valid).toBeFalsy();
    expect(passwordControl?.errors?.['passwordStrength']).toBeTruthy();
  });

  it('should invalidate signup form when passwords dont match', () => {
    const passwordControl = component.signupForm.get('password');
    const retypePaswordControl = component.signupForm.get('retypePassword');
    passwordControl?.setValue('Password123');
    retypePaswordControl?.setValue('Password')
    component.signupForm.updateValueAndValidity()
    expect(component.signupForm.errors?.['identityRevealed']).toBeTruthy();
    passwordControl?.setValue('Password123');
    retypePaswordControl?.setValue('Password123')
    component.signupForm.updateValueAndValidity()
    expect(component.signupForm.errors?.['identityRevealed']).toBeFalsy();
  });

  it('should validate signup form control', () => {
    const emailControl = component.signupForm.get('email');
    const paswordControl = component.signupForm.get('password');
    const retypePaswordControl = component.signupForm.get('retypePassword');
    emailControl?.setValue('');
    paswordControl?.setValue('')
    retypePaswordControl?.setValue('')
    expect(component.signupForm.valid).toBeFalsy();
    emailControl?.setValue('sample@gmail.com');
    paswordControl?.setValue('Password123')
    retypePaswordControl?.setValue('Password123')
    expect(component.signupForm.valid).toBeTruthy();
  });

  it('should validate personal details form control', () => {
    const nameControl = component.personalDetails.get('name');
    const doBControl = component.personalDetails.get('doB');
    const countryControl = component.personalDetails.get('country');
    nameControl?.setValue('');
    doBControl?.setValue('')
    countryControl?.setValue('')
    expect(component.personalDetails.valid).toBeFalsy();
    nameControl?.setValue('Sample User');
    doBControl?.setValue(new Date('2002-09-05'))
    countryControl?.setValue('India')
    expect(component.personalDetails.valid).toBeTruthy();
  });

  it('should invalidate a wrong aadhaar format of govt id', () => {
    const typeControl = component.identificationDetails.get('type');
    const valueControl = component.identificationDetails.get('value');
    typeControl?.setValue('Aadhar');
    valueControl?.setValue('123') //Invalid aadhar format
    component.identificationDetails.updateValueAndValidity()
    expect(component.identificationDetails.errors?.['lengthNotMatched']).toBeTruthy();
    valueControl?.setValue('345674830532')
    component.identificationDetails.updateValueAndValidity() //Correct format
    expect(component.identificationDetails.errors?.['lengthNotMatched']).toBeFalsy();
  });

  it('should validate identification details form control', () => {
    const typeControl = component.identificationDetails.get('type');
    const valueControl = component.identificationDetails.get('value');
    typeControl?.setValue('');
    valueControl?.setValue('')
    expect(component.identificationDetails.valid).toBeFalsy();
    typeControl?.setValue('Aadhar');
    valueControl?.setValue('345674830532')
    expect(component.identificationDetails.valid).toBeTruthy();
  });

  /*REGISTRATION*/
  //There should be an error message when user already exists
  it("should show error snackbar message when registering an existing user - existing email", () => {
    const signupForm = { email: "existing.user@gmail.com", password: 'Password123', retypePassword: 'Password123' };
    component.signupForm.setValue(signupForm);
    const stepper = { next: jasmine.createSpy('next') } as any;
    component.validateNewEmail(stepper); //On submitting signup form
    expect(mockGetClientSpy).toHaveBeenCalled()
    expect(snackBarSpy.open).toHaveBeenCalledWith('User is already registered! Enter a valid email', '', jasmine.any(MatSnackBarConfig));
    expect(component.signupForm.value).toEqual({ email: null, password: null, retypePassword: null });
    expect(stepper.next).not.toHaveBeenCalled();
  });

  //Registration of client - FMTS validation of new user fails
  it("should show error snackbar message when fmts validation of a new user fails", () => {
    const signupForm = { email: "new.user@gmail.com", password: 'Password123', retypePassword: 'Password123' };
    component.signupForm.setValue(signupForm);
    loginMockService.getValidClientDetails.and.returnValue(of(null)) //Valid client
    clientProfileMockService.fmtsClientVerification.and.returnValue(throwError(() => new Error('Couldnt validate user')));
    const stepper = { next: jasmine.createSpy('next') } as any;
    component.validateNewEmail(stepper); //On submitting signup form
    expect(snackBar.open).toHaveBeenCalledWith('Error: Couldnt validate user', '', jasmine.any(Object));
    expect(component.signupForm.value).toEqual({ email: null, password: null, retypePassword: null });
    expect(stepper.next).not.toHaveBeenCalled();
  });

  //Registration of client - Successful Email validation of user
  it("should go to the next after successful validation", () => {
    const signupForm = { email: "sample.user@gmail.com", password: 'Password123', retypePassword: 'Password123' };
    component.signupForm.setValue(signupForm);
    loginMockService.getValidClientDetails.and.returnValue(of(null)) //Valid client
   
    const stepper = { next: jasmine.createSpy('next') } as any;
    component.validateNewEmail(stepper); //On submitting signup form
    expect(mockValidatedClientSpy).toHaveBeenCalled() //Validating client
    expect(stepper.next).toHaveBeenCalled(); //Goes to the next form
  });

  //Registration of client - Invalid govt id
  xit("should show error snackbar message when registering a client with invalid govt id", () => {
    component.identificationDetails.setValue({ type: 'Aadhar', value: '123412341234' });
    component.submitRegistrationForm(); //On submitting registration form
    registerMockService.checkUniqueGovtIDDetails.and.returnValue(throwError(() => new Error('User doesnt have unique id')))
    expect(snackBar.open).toHaveBeenCalledWith('Error: User doesnt have unique id', '', jasmine.any(Object));
  });


  //Successful registration of client
  xit('Successful registration of client',() => {
    component.signupForm.setValue({email:testClient.email, password:testClient.password, retypePassword:testClient.password})
    component.personalDetails.setValue({name:testClient.name, doB:testClient.dateOfBirth, country:testClient.country})
    component.identificationDetails.setValue({ type: testClient.identification[0].type, value: testClient.identification[0].value });
    component.submitRegistrationForm(); //On submitting registration form
    expect(mockUniqueGovtIDSpy).toHaveBeenCalled()
    expect(mockAddClientSpy).toHaveBeenCalledWith(testClient)
    expect(mockAddClientPortfolioSpy).toHaveBeenCalledWith(testClientPortfolio)
    //expect(router.navigateByUrl).toHaveBeenCalledWith('/home/client-preferences');
  })

  

});
