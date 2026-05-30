import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ClientProfile, IsVerifiedClient } from '../../shared/models/client.models';
import { environment } from '../../../environments/environment';

interface LoginRequest {
  email: string;
  password: string;
}

interface RegisterRequest {
  email: string;
  password: string;
  name: string;
  dateOfBirth: string;
  country: string;
  identification: { type: string; value: string }[];
}

@Injectable({ providedIn: 'root' })
export class ClientService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  login(body: LoginRequest): Observable<ClientProfile> {
    return this.http.post<ClientProfile>(`${this.base}/auth/login`, body);
  }

  register(body: RegisterRequest): Observable<ClientProfile> {
    return this.http.post<ClientProfile>(`${this.base}/auth/register`, body);
  }

  refreshToken(): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(`${this.base}/auth/refresh`, {});
  }

  verifyEmail(email: string): Observable<IsVerifiedClient> {
    return this.http.get<IsVerifiedClient>(`${this.base}/client/verify-email/${encodeURIComponent(email)}`);
  }

  ping(): Observable<void> {
    return this.http.get<void>(`${this.base}/client/ping`);
  }
}
