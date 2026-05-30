import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { RoboAdvisorService } from './robo-advisor.service';
import { environment } from '../../../environments/environment';
import { ClientPreferences } from '../../shared/models/client.models';

const MOCK_PREFS: ClientPreferences = {
  clientId:          'c-001',
  investmentPurpose: 'Retirement',
  incomeCategory:    'HIG',
  lengthOfInvestment:'Long',
  percentageOfSpend: 'Tier3',
  riskTolerance:     4,
  acceptAdvisor:     true,
};

describe('RoboAdvisorService', () => {
  let service: RoboAdvisorService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service  = TestBed.inject(RoboAdvisorService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('suggestBuy POSTs preferences to /trade/suggest-buy', () => {
    service.suggestBuy(MOCK_PREFS).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/trade/suggest-buy`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(MOCK_PREFS);
    req.flush([]);
  });

  it('suggestSell POSTs clientId + preferences to /trade/suggest-sell', () => {
    service.suggestSell({ clientId: 'c-001', preferences: MOCK_PREFS }).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/trade/suggest-sell`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ clientId: 'c-001', preferences: MOCK_PREFS });
    req.flush([]);
  });
});
