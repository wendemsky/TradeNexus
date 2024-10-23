import { TestBed } from '@angular/core/testing';

import { ClientActivityReportService } from './client-activity-report.service';

describe('ClientActivityReportService', () => {
  let service: ClientActivityReportService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ClientActivityReportService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
