import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HomeComponent } from './home.component';
import { of } from 'rxjs';
import { ClientProfileService } from 'src/app/services/Client/client-profile.service';
import { ClientPreferencesService } from 'src/app/services/Client/client-preferences.service';
import { MaterialModule } from 'src/app/material.module';
import { provideAnimations } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';

const testClientProfile: any =
{
  "client":
  {
    "id": "688f",
    "email": "rishi@gmail.com",
    "clientId": "1212794226",
    "password": "Marsh2024",
    "name": "Rishiyanth",
    "dateOfBirth": "11/04/2002",
    "country": "India",
    "identification": [
      {
        "type": "Aadhar",
        "value": "123412341234"
      }
    ],
    "isAdmin": false
  },
  "token": 1212670770
}

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

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;

  let clientProfileMockService:any;
  let mockGetClientProfileSpy:any;

  let clientPreferencesMockService:any;
  let mockGetClientPreferencesSpy:any;

  beforeEach(async () => {

    clientProfileMockService = jasmine.createSpyObj('ClientProfileService', ['getClientProfile']);
    mockGetClientProfileSpy = clientProfileMockService.getClientProfile.and.returnValue(of(testClientProfile));

    clientPreferencesMockService = jasmine.createSpyObj('ClientPreferencesService', ['getClientPreferences', 'updateClientPreferences', "setClientPreferences"]);
    mockGetClientPreferencesSpy = clientPreferencesMockService.getClientPreferences.and.returnValue(of(testClientPreferences));

    await TestBed.configureTestingModule({
      declarations: [HomeComponent],
      imports: [
        MaterialModule,
        RouterTestingModule
      ],
      providers: [
        { provide: ClientProfileService, useValue: clientProfileMockService },
        { provide: ClientPreferencesService, useValue: clientPreferencesMockService },
        provideAnimations()
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
