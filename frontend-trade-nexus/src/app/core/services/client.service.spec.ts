import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ClientService } from './client.service';
import { environment } from '../../../environments/environment';

describe('ClientService', () => {
  let service: ClientService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service  = TestBed.inject(ClientService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('login sends POST to /auth/login with credentials', () => {
    service.login({ email: 'a@b.com', password: 'Pass1234' }).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'a@b.com', password: 'Pass1234' });
    req.flush({ client: {}, token: 'tok' });
  });

  it('register sends POST to /auth/register', () => {
    const body = {
      email: 'new@user.com', password: 'Pass1234', name: 'Alice',
      dateOfBirth: '1995-03-20', country: 'India',
      identification: [{ type: 'Aadhar', value: '123456789012' }],
    };
    service.register(body).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/auth/register`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({ client: {}, token: 'tok' });
  });

  it('verifyEmail sends GET to /client/verify-email with encoded email', () => {
    service.verifyEmail('user@example.com').subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/client/verify-email/user%40example.com`);
    expect(req.request.method).toBe('GET');
    req.flush({ isVerified: false });
  });

  it('refreshToken sends POST to /auth/refresh', () => {
    service.refreshToken().subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/auth/refresh`);
    expect(req.request.method).toBe('POST');
    req.flush({ token: 'new-token' });
  });
});
