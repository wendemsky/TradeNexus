import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PortfolioComponent } from './portfolio.component';
import { ClientPortfolioService } from 'src/app/services/Client/client-portfolio.service';
import { of } from 'rxjs';
import { AgGridModule } from 'ag-grid-angular';
import { ClientProfileService } from 'src/app/services/Client/client-profile.service';
import { MaterialModule } from 'src/app/material.module';

const testPortfolioData = {
  "id": "aabd",
  "clientId": "1654658069",
  "currBalance": 10000,
  "holdings": []
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

describe('PortfolioComponent', () => {
  let component: PortfolioComponent;
  let fixture: ComponentFixture<PortfolioComponent>;
  let clientProfileMockService: any = jasmine.createSpyObj('ClientProfileService', ['getClientProfile']);
  let clientPortfolioMockService: any = jasmine.createSpyObj('ClientPortfolioService', ['getClientPortfolio']);
  let getPortfolioSpy = clientPortfolioMockService.getClientPortfolio.and.returnValue(of(testPortfolioData));
  let getProfileSpy = clientProfileMockService.getClientProfile.and.returnValue(of(testClientProfile));

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PortfolioComponent],
      imports: [
        AgGridModule,
        MaterialModule
      ],
      providers: [
        {provide: ClientPortfolioService, useValue: clientPortfolioMockService},
        {provide: ClientProfileService, useValue: clientProfileMockService}
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(PortfolioComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
