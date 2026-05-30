import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ClientPreferences } from '../../shared/models/client.models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class PreferencesService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  getPreferences(clientId: string): Observable<ClientPreferences> {
    return this.http.get<ClientPreferences>(`${this.base}/client-preferences/${clientId}`);
  }

  createPreferences(prefs: ClientPreferences): Observable<ClientPreferences> {
    return this.http.post<ClientPreferences>(`${this.base}/client-preferences`, prefs);
  }

  updatePreferences(prefs: ClientPreferences): Observable<ClientPreferences> {
    return this.http.put<ClientPreferences>(`${this.base}/client-preferences`, prefs);
  }
}
