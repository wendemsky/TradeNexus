import { authStore } from './auth.store';
import { ClientProfile } from '../shared/models/client.models';

const MOCK_PROFILE: ClientProfile = {
  client: {
    clientId: 'c-001',
    email: 'trader@example.com',
    name: 'John Trader',
    dateOfBirth: '1990-05-15',
    country: 'India',
    identification: [{ type: 'Aadhar', value: '123456789012' }],
    isAdmin: false,
  },
  token: 'header.payload.signature',
};

describe('authStore', () => {
  afterEach(() => {
    authStore.logout();
    sessionStorage.clear();
  });

  it('profile is null before any login', () => {
    expect(authStore.profile()).toBeNull();
  });

  it('clientId and isAdmin are null/false before login', () => {
    expect(authStore.clientId()).toBeNull();
    expect(authStore.isAdmin()).toBe(false);
  });

  it('login sets the profile signal', () => {
    authStore.login(MOCK_PROFILE);
    expect(authStore.profile()).toEqual(MOCK_PROFILE);
  });

  it('login persists profile to sessionStorage under tn-session', () => {
    authStore.login(MOCK_PROFILE);
    const raw = sessionStorage.getItem('tn-session');
    expect(raw).not.toBeNull();
    expect(JSON.parse(raw!)).toEqual(MOCK_PROFILE);
  });

  it('clientId is derived from the logged-in profile', () => {
    authStore.login(MOCK_PROFILE);
    expect(authStore.clientId()).toBe('c-001');
  });

  it('isAdmin reflects the client isAdmin flag', () => {
    authStore.login({ ...MOCK_PROFILE, client: { ...MOCK_PROFILE.client, isAdmin: true } });
    expect(authStore.isAdmin()).toBe(true);
  });

  it('logout clears the signal', () => {
    authStore.login(MOCK_PROFILE);
    authStore.logout();
    expect(authStore.profile()).toBeNull();
  });

  it('logout removes the sessionStorage entry', () => {
    authStore.login(MOCK_PROFILE);
    authStore.logout();
    expect(sessionStorage.getItem('tn-session')).toBeNull();
  });

  it('updateToken replaces the token in the signal', () => {
    authStore.login(MOCK_PROFILE);
    authStore.updateToken('new.token.value');
    expect(authStore.profile()?.token).toBe('new.token.value');
  });

  it('updateToken syncs the new token to sessionStorage', () => {
    authStore.login(MOCK_PROFILE);
    authStore.updateToken('refreshed.token.value');
    const stored = JSON.parse(sessionStorage.getItem('tn-session')!);
    expect(stored.token).toBe('refreshed.token.value');
  });

  it('updateToken is a no-op when not logged in', () => {
    authStore.updateToken('anything');
    expect(authStore.profile()).toBeNull();
  });

  it('restoreFromSession returns the stored profile', () => {
    sessionStorage.setItem('tn-session', JSON.stringify(MOCK_PROFILE));
    const restored = authStore.restoreFromSession();
    expect(restored).toEqual(MOCK_PROFILE);
  });

  it('restoreFromSession returns null when sessionStorage is empty', () => {
    expect(authStore.restoreFromSession()).toBeNull();
  });

  it('restoreFromSession returns null and clears corrupted sessionStorage', () => {
    sessionStorage.setItem('tn-session', '{not valid json');
    const restored = authStore.restoreFromSession();
    expect(restored).toBeNull();
    expect(sessionStorage.getItem('tn-session')).toBeNull();
  });
});
