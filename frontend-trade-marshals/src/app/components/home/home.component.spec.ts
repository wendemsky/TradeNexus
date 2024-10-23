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
import { MatDialog } from '@angular/material/dialog';
import { RoboAdvisorComponent } from './_components/robo-advisor/robo-advisor.component';


//Mock Component for Price List
@Component({
  selector: 'app-price-list'
})
class MockPriceListComponent {
}

@Component({
  selector: 'app-robo-advisor'
})
class MockRoboAdvisorComponent {
}

let testClientProfile: any =
{
  "client": {
    "email": "sowmya@gmail.com",
    "clientId": "1654658069",
    "password": "Marsh2024",
    "name": "Sowmya",
    "dateOfBirth": "11/12/2002",
    "country": "India",
    "identification": [
      {
        "type": "Aadhar",
        "value": "123456789102"
      }
    ],
    "isAdmin": true
  },
  "token": 123456789102
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

  let dialogMock: jasmine.SpyObj<MatDialog>;

  beforeEach(async () => {

    clientProfileMockService = jasmine.createSpyObj('ClientProfileService', ['getClientProfile']);
    mockGetClientProfileSpy = clientProfileMockService.getClientProfile.and.returnValue(of(testClientProfile));

    clientPreferencesMockService = jasmine.createSpyObj('ClientPreferencesService', ['getClientPreferences', 'updateClientPreferences', "setClientPreferences"]);
    mockGetClientPreferencesSpy = clientPreferencesMockService.getClientPreferences.and.returnValue(of(testClientPreferences));

    dialogMock = jasmine.createSpyObj('MatDialog', ['open']);

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
        { provide: MatDialog, useValue: dialogMock },
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

  it('should render not robo advisor component if client preferences accept advisor t&c is set to false', () => {
    testClientPreferences.acceptAdvisor = false;
    clientPreferencesMockService.getClientPreferences.and.returnValue(of(testClientPreferences));
    fixture.detectChanges()
    const roboAdvisorIcon = fixture.debugElement.query(By.css('.chatbot-button'));
    expect(roboAdvisorIcon).toBeFalsy();
  })

  it('should render robo advisor component if client preferences accept advisor t&c is set to true', () => {
    testClientPreferences.acceptAdvisor = true;
    clientPreferencesMockService.getClientPreferences.and.returnValue(of(testClientPreferences));
    fixture.detectChanges()
    const roboAdvisorIcon = fixture.debugElement.query(By.css('.chatbot-button'));
    expect(roboAdvisorIcon).toBeTruthy();
  })

  it('should open a dialog on clicking robo advisor icon', () => {
    testClientPreferences.acceptAdvisor = true;
    clientPreferencesMockService.getClientPreferences.and.returnValue(of(testClientPreferences));
    fixture.detectChanges()
    //Triggering the clicking of the chatbot icon
    const roboAdvisorIcon = fixture.debugElement.query(By.css('.chatbot-button'));
    roboAdvisorIcon.triggerEventHandler('click', null);
    fixture.detectChanges();
    expect(dialogMock.open).toHaveBeenCalledWith(RoboAdvisorComponent, {
      height: '65%',
      width: '80%',
    });
  });

});
