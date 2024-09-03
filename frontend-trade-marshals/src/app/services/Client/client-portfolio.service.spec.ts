import { TestBed } from '@angular/core/testing';

import { ClientPortfolioService } from './client-portfolio.service';

describe('ClientPortfolioService', () => {
  let service: ClientPortfolioService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ClientPortfolioService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
