import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { authStore } from '../../store/auth.store';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = authStore.profile()?.token;

  const authedReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authedReq).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401) {
        authStore.logout();
        inject(Router).navigate(['/']);
      }
      return throwError(() => err);
    })
  );
};
