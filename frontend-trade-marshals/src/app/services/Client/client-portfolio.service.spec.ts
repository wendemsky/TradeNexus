import { TestBed } from '@angular/core/testing';

import { ClientPortfolioService } from './client-portfolio.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('ClientPortfolioService', () => {
  let service: ClientPortfolioService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(ClientPortfolioService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
