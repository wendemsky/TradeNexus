import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TradeService } from './trade.service';
import { environment } from '../../../environments/environment';
import { Order } from '../../shared/models/trade.models';

const MARKET_ORDER: Order = {
  orderId:      'order-uuid-001',
  instrumentId: 'AAPL',
  quantity:     5,
  targetPrice:  null,
  direction:    'B',
  orderType:    'MARKET',
  clientId:     'c-001',
  token:        'bearer.token.value',
};

const LIMIT_ORDER: Order = {
  ...MARKET_ORDER,
  orderId:     'order-uuid-002',
  orderType:   'LIMIT',
  targetPrice: 180,
  direction:   'S',
};

describe('TradeService', () => {
  let service: TradeService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service  = TestBed.inject(TradeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('executeTrade POSTs MARKET order to /trade/execute-trade', () => {
    service.executeTrade(MARKET_ORDER).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/trade/execute-trade`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(MARKET_ORDER);
    expect(req.request.body.orderType).toBe('MARKET');
    expect(req.request.body.targetPrice).toBeNull();
    req.flush({});
  });

  it('executeTrade POSTs LIMIT order with targetPrice set', () => {
    service.executeTrade(LIMIT_ORDER).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/trade/execute-trade`);
    expect(req.request.body.orderType).toBe('LIMIT');
    expect(req.request.body.targetPrice).toBe(180);
    req.flush({});
  });

  it('getTradeHistory GETs /trade/trade-history/:clientId', () => {
    service.getTradeHistory('c-001').subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/trade/trade-history/c-001`);
    expect(req.request.method).toBe('GET');
    req.flush({ clientId: 'c-001', trades: [] });
  });
});
