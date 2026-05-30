import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { authStore } from '../../store/auth.store';
import { TokenService } from './token.service';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const tokenService = inject(TokenService);
  const token = authStore.profile()?.token;

  if (!token || tokenService.isExpired(token)) {
    authStore.logout();
    return router.createUrlTree(['/']);
  }

  return true;
};
