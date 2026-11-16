import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap, throwError } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { LoginRequest, LoginResponse, RegisterRequest, UserResponse } from '../models/auth.models';

const ACCESS_TOKEN_KEY = 'cine_access_token';
const REFRESH_TOKEN_KEY = 'cine_refresh_token';
const USER_KEY = 'cine_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly accessTokenSignal = signal<string | null>(localStorage.getItem(ACCESS_TOKEN_KEY));
  private readonly currentUserSignal = signal<UserResponse | null>(this.restoreUser());

  readonly accessToken = computed(() => this.accessTokenSignal());
  readonly currentUser = computed(() => this.currentUserSignal());
  readonly isAuthenticated = computed(() => !!this.accessTokenSignal());
  readonly isAdmin = computed(() => this.currentUserSignal()?.role === 'ADMIN');

  constructor() {
    this.syncSessionState();
  }

  login(payload: LoginRequest) {
    return this.http.post<LoginResponse>(`${API_BASE_URL}/api/auth/login`, payload).pipe(
      tap((response) => {
        this.persistSession(response);
      })
    );
  }

  register(payload: RegisterRequest) {
    return this.http.post<UserResponse>(`${API_BASE_URL}/api/auth/register`, payload);
  }

  updateProfile(payload: RegisterRequest) {
    return this.http.put<UserResponse>(`${API_BASE_URL}/api/users/me`, payload).pipe(
      tap((response) => {
        localStorage.setItem(USER_KEY, JSON.stringify(response));
        this.currentUserSignal.set(response);
      })
    );
  }

  getCurrentUser() {
    return this.http.get<UserResponse>(`${API_BASE_URL}/api/users/me`);
  }

  refreshAccessToken(): Observable<LoginResponse> {
    const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
    if (!refreshToken) {
      return throwError(() => new Error('No refresh token available'));
    }

    return this.http.post<LoginResponse>(`${API_BASE_URL}/api/auth/refresh`, null, {
      headers: { Authorization: `Bearer ${refreshToken}` }
    }).pipe(
      tap((response) => this.persistSession(response))
    );
  }

  logout() {
    const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);

    if (refreshToken) {
      // Best-effort: revoke server-side even though we clear the local session regardless.
      this.http.post(`${API_BASE_URL}/api/auth/logout`, null, {
        headers: { Authorization: `Bearer ${refreshToken}` }
      }).subscribe({ error: () => {} });
    }

    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.accessTokenSignal.set(null);
    this.currentUserSignal.set(null);
    this.router.navigate(['/auth/login']);
  }

  private persistSession(response: LoginResponse) {
    localStorage.setItem(ACCESS_TOKEN_KEY, response.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken);
    localStorage.setItem(USER_KEY, JSON.stringify(response.user));

    this.accessTokenSignal.set(response.accessToken);
    this.currentUserSignal.set(response.user);
  }

  private restoreUser(): UserResponse | null {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) {
      return null;
    }

    try {
      return JSON.parse(raw) as UserResponse;
    } catch {
      localStorage.removeItem(USER_KEY);
      return null;
    }
  }

  private syncSessionState() {
    const token = this.accessTokenSignal();
    if (!token || this.isAccessTokenExpired(token)) {
      this.clearStoredSession();
      this.accessTokenSignal.set(null);
      this.currentUserSignal.set(null);
      return;
    }

    this.currentUserSignal.set(this.restoreUser());
  }

  private clearStoredSession() {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }

  private isAccessTokenExpired(token: string): boolean {
    try {
      const [, payloadPart] = token.split('.');
      if (!payloadPart) {
        return true;
      }

      const payload = JSON.parse(atob(payloadPart.replace(/-/g, '+').replace(/_/g, '/'))) as { exp?: number };
      if (!payload.exp) {
        return false;
      }

      const nowInSeconds = Math.floor(Date.now() / 1000);
      return payload.exp <= nowInSeconds;
    } catch {
      return true;
    }
  }
}
