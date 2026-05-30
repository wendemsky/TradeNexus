import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivityService } from './activity.service';
import { environment } from '../../../environments/environment';
import { Holding } from '../../shared/models/client.models';
import { Trade, TradePL } from '../../shared/models/trade.models';

describe('ActivityService', () => {
  let service: ActivityService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service  = TestBed.inject(ActivityService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getHoldings sends GET to /activity-report/holdings/:clientId', () => {
    const mockHoldings: Holding[] = [
      { instrumentId: 'AAPL', instrumentDescription: 'Apple Inc', categoryId: 'STOCK', quantity: 10, avgPrice: 150 },
    ];
    service.getHoldings('c-001').subscribe(data => {
      expect(data).toEqual(mockHoldings);
    });
    const req = httpMock.expectOne(`${environment.apiUrl}/activity-report/holdings/c-001`);
    expect(req.request.method).toBe('GET');
    req.flush(mockHoldings);
  });

  it('getTrades sends GET to /activity-report/trades/:clientId', () => {
    service.getTrades('c-001').subscribe(data => {
      expect(data.trades).toEqual([]);
    });
    const req = httpMock.expectOne(`${environment.apiUrl}/activity-report/trades/c-001`);
    expect(req.request.method).toBe('GET');
    req.flush({ clientId: 'c-001', trades: [] });
  });

  it('getPL sends GET to /activity-report/pl/:clientId', () => {
    const mockPL: TradePL[] = [
      { instrumentId: 'AAPL', instrumentDescription: 'Apple Inc', categoryId: 'STOCK', realizedPL: 100, unrealizedPL: 50, totalPL: 150 },
    ];
    service.getPL('c-001').subscribe(data => {
      expect(data).toEqual(mockPL);
    });
    const req = httpMock.expectOne(`${environment.apiUrl}/activity-report/pl/c-001`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPL);
  });
});
