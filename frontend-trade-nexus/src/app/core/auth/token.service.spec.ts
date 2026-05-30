import { TestBed } from '@angular/core/testing';
import { TokenService } from './token.service';

function makeToken(expOffsetSeconds: number): string {
  const now = Math.floor(Date.now() / 1000);
  const header  = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const payload = btoa(JSON.stringify({ sub: 'u1', exp: now + expOffsetSeconds }));
  return `${header}.${payload}.fakesig`;
}

describe('TokenService', () => {
  let service: TokenService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TokenService);
  });

  describe('isExpired', () => {
    it('returns false for a token expiring in the future', () => {
      expect(service.isExpired(makeToken(3600))).toBe(false);
    });

    it('returns true for an already-expired token', () => {
      expect(service.isExpired(makeToken(-1))).toBe(true);
    });

    it('returns true for a malformed token', () => {
      expect(service.isExpired('not.a.jwt')).toBe(true);
    });

    it('returns true for an empty string', () => {
      expect(service.isExpired('')).toBe(true);
    });
  });

  describe('shouldRefresh', () => {
    it('returns false when more than 5 minutes remain', () => {
      expect(service.shouldRefresh(makeToken(600))).toBe(false);
    });

    it('returns true when within 5 minutes of expiry', () => {
      expect(service.shouldRefresh(makeToken(240))).toBe(true);
    });

    it('returns true for an already-expired token', () => {
      expect(service.shouldRefresh(makeToken(-1))).toBe(true);
    });

    it('returns false for a malformed token', () => {
      expect(service.shouldRefresh('bad.token')).toBe(false);
    });
  });
});
