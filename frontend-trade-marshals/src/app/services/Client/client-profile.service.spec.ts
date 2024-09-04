import { TestBed } from '@angular/core/testing';

import { ClientProfileService } from './client-profile.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('ClientProfileService', () => {
  let service: ClientProfileService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(ClientProfileService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
