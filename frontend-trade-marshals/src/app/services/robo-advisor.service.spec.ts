import { TestBed } from '@angular/core/testing';

import { RoboAdvisorService } from './robo-advisor.service';

describe('RoboAdvisorService', () => {
  let service: RoboAdvisorService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RoboAdvisorService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
