import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { PreferencesService } from './preferences.service';
import { environment } from '../../../environments/environment';
import { ClientPreferences } from '../../shared/models/client.models';

const MOCK_PREFS: ClientPreferences = {
  clientId:          'c-001',
  investmentPurpose: 'Education',
  incomeCategory:    'MIG',
  lengthOfInvestment:'Medium',
  percentageOfSpend: 'Tier2',
  riskTolerance:     3,
  acceptAdvisor:     false,
};

describe('PreferencesService', () => {
  let service: PreferencesService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service  = TestBed.inject(PreferencesService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getPreferences sends GET to /client-preferences/:clientId', () => {
    service.getPreferences('c-001').subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/client-preferences/c-001`);
    expect(req.request.method).toBe('GET');
    req.flush(MOCK_PREFS);
  });

  it('createPreferences sends POST to /client-preferences', () => {
    service.createPreferences(MOCK_PREFS).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/client-preferences`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(MOCK_PREFS);
    req.flush(MOCK_PREFS);
  });

  it('updatePreferences sends PUT to /client-preferences', () => {
    service.updatePreferences(MOCK_PREFS).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/client-preferences`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(MOCK_PREFS);
    req.flush(MOCK_PREFS);
  });
});
