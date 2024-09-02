import { TestBed } from '@angular/core/testing';

import { ClientPreferencesService } from './client-preferences.service';

describe('ClientPreferencesService', () => {
  let service: ClientPreferencesService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ClientPreferencesService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
