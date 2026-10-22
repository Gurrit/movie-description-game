const KEY = 'mdg.token';

// Used when localStorage is unavailable (private windows, blocked site data): the player then
// keeps their progress only until the page is closed.
let memoryToken: string | null = null;
let persistent = true;

export function getToken(): string | null {
  try {
    return localStorage.getItem(KEY) ?? memoryToken;
  } catch {
    return memoryToken;
  }
}

export function setToken(token: string): void {
  memoryToken = token;
  try {
    localStorage.setItem(KEY, token);
    persistent = true;
  } catch {
    persistent = false;
  }
}

export function clearToken(): void {
  memoryToken = null;
  try {
    localStorage.removeItem(KEY);
  } catch {
    // nothing to clear
  }
}

/** False once saving the token failed, so the UI can warn that progress will not survive a reload. */
export function isTokenPersistent(): boolean {
  return persistent;
}
