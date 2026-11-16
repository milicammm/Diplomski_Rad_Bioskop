import { HttpErrorResponse, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { BehaviorSubject, catchError, filter, switchMap, take, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

let isRefreshing = false;
const refreshedAccessToken$ = new BehaviorSubject<string | null>(null);

const NO_CACHE_HEADERS = {
  'Cache-Control': 'no-cache, no-store, must-revalidate',
  'Pragma': 'no-cache',
  'Expires': '0'
};

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  return next(withAuthHeader(req, authService.accessToken())).pipe(
    catchError((error) => {
      if (error instanceof HttpErrorResponse && error.status === 401 && !isAuthEndpoint(req.url)) {
        return handleUnauthorized(req, next, authService);
      }

      return throwError(() => error);
    })
  );
};

function handleUnauthorized(req: HttpRequest<unknown>, next: HttpHandlerFn, authService: AuthService) {
  if (!authService.accessToken()) {
    return throwError(() => new HttpErrorResponse({ status: 401 }));
  }

  if (!isRefreshing) {
    isRefreshing = true;
    refreshedAccessToken$.next(null);

    return authService.refreshAccessToken().pipe(
      switchMap((response) => {
        isRefreshing = false;
        refreshedAccessToken$.next(response.accessToken);
        return retryWithToken(req, next, response.accessToken, authService);
      }),
      catchError((refreshError) => {
        isRefreshing = false;
        authService.logout();
        return throwError(() => refreshError);
      })
    );
  }

  return refreshedAccessToken$.pipe(
    filter((token): token is string => token !== null),
    take(1),
    switchMap((token) => retryWithToken(req, next, token, authService))
  );
}

function retryWithToken(req: HttpRequest<unknown>, next: HttpHandlerFn, token: string, authService: AuthService) {
  return next(withAuthHeader(req, token)).pipe(
    catchError((retryError) => {
      if (retryError instanceof HttpErrorResponse && retryError.status === 401) {
        authService.logout();
      }

      return throwError(() => retryError);
    })
  );
}

function withAuthHeader(req: HttpRequest<unknown>, token: string | null) {
  const headers: Record<string, string> = { ...NO_CACHE_HEADERS };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  return req.clone({ setHeaders: headers });
}

function isAuthEndpoint(url: string) {
  return url.includes('/api/auth/login') || url.includes('/api/auth/register') || url.includes('/api/auth/refresh');
}
