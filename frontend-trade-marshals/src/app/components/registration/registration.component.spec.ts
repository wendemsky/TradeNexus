import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegistrationComponent } from './registration.component';
import { DatePipe } from '@angular/common';
import { MaterialModule } from 'src/app/material.module';
import { of } from 'rxjs';
import { LoginService } from 'src/app/services/Client/login.service';
import { RegisterService } from 'src/app/services/Client/register.service';
import { ClientProfileService } from 'src/app/services/Client/client-profile.service';
import { ReactiveFormsModule } from '@angular/forms';
import { provideAnimations } from '@angular/platform-browser/animations';

const testClient = {
  email: "himanshu@gmail.com",
  clientId: "541107416",
  password: "Marsh2024",
  name: "Himanshu",
  dateOfBirth: "08/12/2002",
  country: "India",
  isAdmin: true

}

const testClientProfile: any = 
{
  "client": 
  {
    "id":"688f",
    "email":"rishi@gmail.com",
    "clientId":"1212794226",
    "password":"Marsh2024",
    "name":"Rishiyanth",
    "dateOfBirth":"11/04/2002",
    "country":"India",
    "identification":[
      {
        "type":"Aadhar",
        "value":"123412341234"
      }
    ],
    "isAdmin":false
  },
  "token":1212670770
}

let loginMockService: any = jasmine.createSpyObj('LoginService', ['getValidClientDetails']);
loginMockService.getValidClientDetails.and.returnValue(of(testClient));

let registrationMockService: any = jasmine.createSpyObj('RegisterService', ['saveClientDetails', 'saveClientPortfolioDetails']);

let clientProfileMockService: any = jasmine.createSpyObj('ClientProfileService', ['getClientProfile']);
clientProfileMockService.getClientProfile.and.returnValue(of(testClientProfile));

describe('RegistrationComponent', () => {
  let component: RegistrationComponent;
  let fixture: ComponentFixture<RegistrationComponent>;
  let datePipe: DatePipe

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RegistrationComponent,],
      imports: [
        MaterialModule,
        ReactiveFormsModule
      ],
      providers: [
        DatePipe,
        {provide: LoginService, useValue: loginMockService },
        {provide: RegisterService, useValue: registrationMockService },
        {provide: ClientProfileService, useValue: clientProfileMockService },
        provideAnimations() 
      ]
    })
      .compileComponents();

    datePipe = TestBed.inject(DatePipe)
    fixture = TestBed.createComponent(RegistrationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
