import { TestBed } from '@angular/core/testing';

import { TradeHistoryService } from './trade-history.service';

describe('TradeHistoryService', () => {
  let service: TradeHistoryService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TradeHistoryService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
