import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ClientPreferencesService } from './client-preferences.service';
import { ClientPreferences } from 'src/app/models/Client/ClientPreferences';
import { HttpErrorResponse } from '@angular/common/http';

describe('ClientPreferencesService', () => {
  let service: ClientPreferencesService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ClientPreferencesService]
    });
    service = TestBed.inject(ClientPreferencesService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch client preferences', () => {
    const mockPreferences: any[] = [
      { clientId: '1654658069', investmentPurpose: 'Retirement', incomeCategory: 'MIG', lengthOfInvestment: 'Medium', percentageOfSpend: 'Tier2', riskTolerance: 3, acceptAdvisor: 'true' }
    ];

    const req = httpMock.expectOne(service.dataURL+'1654658069');
    expect(req.request.method).toBe('GET');
    req.flush(mockPreferences);

    service.getClientPreferences('1654658069').subscribe(preferences => {
      expect(preferences).toEqual(mockPreferences[0]);
    });

   
  });

  it('should handle error when fetching client preferences', () => {
    const errorMessage = 'Unexpected error at service while trying to login user. Please try again later!';

    service.getClientPreferences('123').subscribe(
      () => fail('expected an error, not preferences'),
      error => expect(error).toBe(errorMessage)
    );

    const req = httpMock.expectOne(service.dataURL);
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should update client preferences', () => {
    const mockPreferences: any = { clientId: '1654658069', investmentPurpose: 'Retirement', incomeCategory: 'MIG', lengthOfInvestment: 'Medium', percentageOfSpend: 'Tier2', riskTolerance: 3, acceptAdvisor: 'true' };

    service.updateClientPreferences(mockPreferences).subscribe(preferences => {
      expect(preferences).toEqual(mockPreferences);
    });

    const req = httpMock.expectOne(`${service.dataURL}`);
    expect(req.request.method).toBe('PUT');
    req.flush(mockPreferences);
  });

  it('should handle error when updating client preferences', () => {
    const errorMessage = 'Unexpected error at service while trying to login user. Please try again later!';
    const mockPreferences: any = { clientId: '1654658069', investmentPurpose: 'Retirement', incomeCategory: 'MIG', lengthOfInvestment: 'Medium', percentageOfSpend: 'Tier2', riskTolerance: 3, acceptAdvisor: 'true' };
    service.updateClientPreferences(mockPreferences).subscribe(
      () => fail('expected an error, not preferences'),
      error => expect(error).toBe(errorMessage)
    );

    const req = httpMock.expectOne(`${service.dataURL}123`);
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should set client preferences', () => {
    const mockPreferences: any = { clientId: '1654658069', investmentPurpose: 'Retirement', incomeCategory: 'MIG', lengthOfInvestment: 'Medium', percentageOfSpend: 'Tier2', riskTolerance: 3, acceptAdvisor: 'true' };

    service.setClientPreferences(mockPreferences).subscribe(preferences => {
      expect(preferences).toEqual(mockPreferences);
    });

    const req = httpMock.expectOne(service.dataURL);
    expect(req.request.method).toBe('POST');
    req.flush(mockPreferences);
  });
});
