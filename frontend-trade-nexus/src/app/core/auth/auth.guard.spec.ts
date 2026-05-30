import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { provideRouter } from '@angular/router';
import { authGuard } from './auth.guard';
import { authStore } from '../../store/auth.store';
import { Client, ClientProfile } from '../../shared/models/client.models';

function makeToken(expOffsetSeconds: number): string {
  const now = Math.floor(Date.now() / 1000);
  const header  = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const payload = btoa(JSON.stringify({ sub: 'u1', exp: now + expOffsetSeconds }));
  return `${header}.${payload}.fakesig`;
}

const MOCK_CLIENT: Client = {
  clientId: 'c-001',
  email: 'trader@example.com',
  name: 'Test',
  dateOfBirth: '1990-01-01',
  country: 'India',
  identification: [],
  isAdmin: false,
};

describe('authGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([])],
    });
  });

  afterEach(() => authStore.logout());

  it('allows navigation when token is valid', () => {
    authStore.login({ client: MOCK_CLIENT, token: makeToken(3600) });
    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as any, {} as any)
    );
    expect(result).toBe(true);
  });

  it('redirects to / when no session exists', () => {
    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as any, {} as any)
    );
    const router = TestBed.inject(Router);
    expect(result).toEqual(router.createUrlTree(['/']));
  });

  it('redirects to / and clears session when token is expired', () => {
    authStore.login({ client: MOCK_CLIENT, token: makeToken(-60) });
    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as any, {} as any)
    );
    const router = TestBed.inject(Router);
    expect(result).toEqual(router.createUrlTree(['/']));
    expect(authStore.profile()).toBeNull();
  });
});
