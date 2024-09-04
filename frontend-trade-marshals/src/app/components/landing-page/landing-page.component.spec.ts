import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LandingPageComponent } from './landing-page.component';
import { MatDialogRef } from '@angular/material/dialog';
import { MaterialModule } from 'src/app/material.module';
import { of } from 'rxjs';
import { LoginService } from 'src/app/services/Client/login.service';
import { ClientProfileService } from 'src/app/services/Client/client-profile.service';

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

let clientProfileMockService: any = jasmine.createSpyObj('ClientProfileService', ['getClientProfile']);
clientProfileMockService.getClientProfile.and.returnValue(of(testClientProfile));

describe('LandingPageComponent', () => {
  let component: LandingPageComponent;
  let fixture: ComponentFixture<LandingPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LandingPageComponent],
      imports: [
        MaterialModule
      ],
      providers: [
        {provide: LoginService, useValue: loginMockService},
        {provide: ClientProfileService, useValue: clientProfileMockService},
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(LandingPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
