import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { authStore } from '../../store/auth.store';
import { ClientProfile } from '../../shared/models/client.models';

const MOCK_PROFILE: ClientProfile = {
  client: {
    clientId: 'c-001',
    email: 'trader@example.com',
    name: 'Test',
    dateOfBirth: '1990-01-01',
    country: 'India',
    identification: [],
    isAdmin: false,
  },
  token: 'valid.test.token',
};

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    authStore.logout();
  });

  it('attaches Authorization header when a session exists', () => {
    authStore.login(MOCK_PROFILE);
    http.get('/api/test').subscribe();
    const req = httpMock.expectOne('/api/test');
    expect(req.request.headers.get('Authorization')).toBe('Bearer valid.test.token');
    req.flush({});
  });

  it('sends request without Authorization header when no session', () => {
    http.get('/api/test').subscribe();
    const req = httpMock.expectOne('/api/test');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('clears the session on a 401 response', () => {
    authStore.login(MOCK_PROFILE);
    http.get('/api/protected').subscribe({ error: () => {} });
    const req = httpMock.expectOne('/api/protected');
    req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });
    expect(authStore.profile()).toBeNull();
  });

  it('does not clear the session on a non-401 error', () => {
    authStore.login(MOCK_PROFILE);
    http.get('/api/data').subscribe({ error: () => {} });
    const req = httpMock.expectOne('/api/data');
    req.flush({ message: 'Not Found' }, { status: 404, statusText: 'Not Found' });
    expect(authStore.profile()).not.toBeNull();
  });
});
