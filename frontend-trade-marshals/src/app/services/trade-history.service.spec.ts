import { TestBed } from '@angular/core/testing';

import { TradeHistoryService } from './trade-history.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MaterialModule } from '../material.module';

describe('TradeHistoryService', () => {
  let service: TradeHistoryService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        MaterialModule
      ]
    });
    service = TestBed.inject(TradeHistoryService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
