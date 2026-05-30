import { Injectable } from '@angular/core';
import { jwtDecode } from 'jwt-decode';

interface JwtPayload {
  sub: string;
  email: string;
  isAdmin: boolean;
  exp: number;
  iat: number;
}

@Injectable({ providedIn: 'root' })
export class TokenService {
  isExpired(token: string): boolean {
    try {
      const { exp } = jwtDecode<JwtPayload>(token);
      return Date.now() >= exp * 1000;
    } catch {
      return true;
    }
  }

  shouldRefresh(token: string): boolean {
    try {
      const { exp } = jwtDecode<JwtPayload>(token);
      return Date.now() >= exp * 1000 - 5 * 60_000;
    } catch {
      return false;
    }
  }
}
