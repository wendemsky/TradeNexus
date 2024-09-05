import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClientPreferencesComponent } from './client-preferences.component';
import { of } from 'rxjs';
import { ClientPreferencesService } from 'src/app/services/Client/client-preferences.service';
import { ClientProfileService } from 'src/app/services/Client/client-profile.service';

import { MaterialModule } from 'src/app/material.module';
import { ReactiveFormsModule } from '@angular/forms';
import { provideAnimations } from '@angular/platform-browser/animations';

const testClientPreferences: any = 
  {
    "investmentPurpose": "Education",
    "incomeCategory": "HIG",
    "lengthOfInvestment": "Short",
    "percentageOfSpend": "Tier4",
    "riskTolerance": 2,
    "acceptAdvisor": false,
    "clientId": "1654658069",
    "id": "15b4"
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


let clientPreferencesMockService: any = jasmine.createSpyObj('ClientPreferencesService', ['getClientPreferences', 'updateClientPreferences', "setClientPreferences"]);
clientPreferencesMockService.getClientPreferences.and.returnValue(of(testClientPreferences));

let clientProfileMockService: any = jasmine.createSpyObj('ClientProfileService', ['getClientProfile']);
clientProfileMockService.getClientProfile.and.returnValue(of(testClientProfile));

describe('ClientPreferencesComponent', () => {
  let component: ClientPreferencesComponent;
  let fixture: ComponentFixture<ClientPreferencesComponent>;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ 
        ClientPreferencesComponent,
      ],
      imports: [
        MaterialModule,
        ReactiveFormsModule
      ],
      providers: [
        {provide: ClientPreferencesService, useValue: clientPreferencesMockService},
        {provide: ClientProfileService, useValue: clientProfileMockService},
        provideAnimations() 
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClientPreferencesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
