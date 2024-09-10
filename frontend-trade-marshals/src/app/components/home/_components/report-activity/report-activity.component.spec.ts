import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReportActivityComponent } from './report-activity.component';
import { MaterialModule } from 'src/app/material.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { provideAnimations } from '@angular/platform-browser/animations';
import { ClientProfileService } from 'src/app/services/Client/client-profile.service';
import { of } from 'rxjs';

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

describe('ReportActivityComponent', () => {
  let component: ReportActivityComponent;
  let fixture: ComponentFixture<ReportActivityComponent>;

  let clientProfileMockService:any;
  let mockGetClientProfileSpy:any;

  beforeEach(async () => {

    clientProfileMockService = jasmine.createSpyObj('ClientProfileService', ['getClientProfile']);
    mockGetClientProfileSpy = clientProfileMockService.getClientProfile.and.returnValue(of(testClientProfile));

    await TestBed.configureTestingModule({
      declarations: [ ReportActivityComponent ],
      imports: [
        MaterialModule,
        ReactiveFormsModule
      ],
      providers: [
        provideAnimations(),
        { provide: ClientProfileService, useValue: clientProfileMockService },
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReportActivityComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should log the report value to the console on submit', () => {
    // Spy on console.log
    spyOn(console, 'log');

    // Call the onSubmit method
    component.onSubmit();

    // Check that console.log was called with the correct arguments
    expect(console.log).toHaveBeenCalledWith('Report type -> ' + JSON.stringify(component.report.value));
  });
  
});
