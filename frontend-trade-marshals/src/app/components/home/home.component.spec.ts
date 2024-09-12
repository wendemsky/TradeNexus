import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HomeComponent } from './home.component';
import { of } from 'rxjs';
import { ClientProfileService } from 'src/app/services/Client/client-profile.service';
import { ClientPreferencesService } from 'src/app/services/Client/client-preferences.service';
import { MaterialModule } from 'src/app/material.module';
import { provideAnimations } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { Component } from '@angular/core';
import { By } from '@angular/platform-browser';
import { Router } from '@angular/router';
import {Location} from '@angular/common'
import {routes} from '../../app-routing.module'


//Mock Component for Price List
@Component({
  selector: 'app-price-list'
})
class MockPriceListComponent {
}

//Mock Component for Robo Advisor
@Component({
  selector: 'app-robo-advisor'
})
class MockRoboAdvisorComponent {
}

let testClientProfile: any =
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

let testClientPreferences: any =
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
  let router: Router;
  let location: Location;

  let clientProfileMockService: any;
  let mockGetClientProfileSpy: any;

  let clientPreferencesMockService: any;
  let mockGetClientPreferencesSpy: any;

  beforeEach(async () => {

    clientProfileMockService = jasmine.createSpyObj('ClientProfileService', ['getClientProfile']);
    mockGetClientProfileSpy = clientProfileMockService.getClientProfile.and.returnValue(of(testClientProfile));

    clientPreferencesMockService = jasmine.createSpyObj('ClientPreferencesService', ['getClientPreferences', 'updateClientPreferences', "setClientPreferences"]);
    mockGetClientPreferencesSpy = clientPreferencesMockService.getClientPreferences.and.returnValue(of(testClientPreferences));

    await TestBed.configureTestingModule({
      declarations: [HomeComponent,
        MockPriceListComponent,
        MockRoboAdvisorComponent
      ],
      imports: [
        MaterialModule,
        RouterTestingModule.withRoutes(routes)
      ],
      providers: [
        { provide: ClientProfileService, useValue: clientProfileMockService },
        { provide: ClientPreferencesService, useValue: clientPreferencesMockService },
        provideAnimations()
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    
    router = TestBed.inject(Router);
    location = TestBed.inject(Location)
    await router.navigateByUrl('/home');
    fixture.detectChanges();

    component = fixture.componentInstance;
    fixture.detectChanges();

  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should retrieve logged in client profile details', () => {
    expect(mockGetClientProfileSpy).toHaveBeenCalled();
    expect(component.clientProfileData).toBe(testClientProfile);
  })

  it('should retrieve logged in client preferences details', () => {
    expect(mockGetClientPreferencesSpy).toHaveBeenCalled();
    expect(component.clientPreferencesData).toBe(testClientPreferences);
  })

  it('should render home content', () => {
    expect(location.path()).toBe('/home')
    expect(component.isHomeContent).toBeTruthy();
  })

  it('should render robo advisor component if client preferences accept advisor t&c is set to true', () => {
    testClientPreferences.acceptAdvisor = true;
    clientPreferencesMockService.getClientPreferences.and.returnValue(of(testClientPreferences));
    fixture.detectChanges()
    expect(component.clientPreferencesData).toBe(testClientPreferences)
    const roboAdvisorComponent = fixture.debugElement.query(By.directive(MockRoboAdvisorComponent));
    expect(roboAdvisorComponent).toBeTruthy();
  })

});
