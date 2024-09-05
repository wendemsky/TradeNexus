import { TestBed } from '@angular/core/testing';

import { ClientPreferencesService } from './client-preferences.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';


describe('ClientPreferencesService', () => {
  let service: ClientPreferencesService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(ClientPreferencesService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
