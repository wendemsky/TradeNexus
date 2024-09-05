import { TestBed } from '@angular/core/testing';

import { TradeService } from './trade.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('TradeService', () => {
  let service: TradeService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(TradeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
