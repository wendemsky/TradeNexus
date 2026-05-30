import { computed, signal } from '@angular/core';
import { ClientProfile } from '../shared/models/client.models';

const SESSION_KEY = 'tn-session';

const _profile = signal<ClientProfile | null>(null);

export const authStore = {
  profile:  _profile.asReadonly(),
  clientId: computed(() => _profile()?.client.clientId ?? null),
  isAdmin:  computed(() => _profile()?.client.isAdmin ?? false),

  login(profile: ClientProfile): void {
    _profile.set(profile);
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(profile));
  },

  logout(): void {
    _profile.set(null);
    sessionStorage.removeItem(SESSION_KEY);
  },

  updateToken(token: string): void {
    const current = _profile();
    if (!current) return;
    const updated: ClientProfile = { ...current, token };
    _profile.set(updated);
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(updated));
  },

  restoreFromSession(): ClientProfile | null {
    const raw = sessionStorage.getItem(SESSION_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as ClientProfile;
    } catch {
      sessionStorage.removeItem(SESSION_KEY);
      return null;
    }
  },
};
