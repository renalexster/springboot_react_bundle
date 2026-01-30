import { apiFetch, API_BASE } from './api'

export function getToken(): string | null {
  return localStorage.getItem('google_id_token')
}

export function setToken(token: string): void {
  localStorage.setItem('google_id_token', token)
}

export function clearToken(): void {
  localStorage.removeItem('google_id_token')
}

export async function verifyToken(token: string): Promise<{ name?: string }>{
  const res = await apiFetch(`${API_BASE}/api/auth/verify`, {
    method: 'GET',
    headers: {
      Authorization: `Bearer ${token}`,
    },
    credentials: 'include',
  })
  if (!res.ok) {
    const text = await res.text()
    throw new Error(text || `Request failed: ${res.status}`)
  }
  return res.json()
}

export function logout(): void {
  clearToken()
  if (window.google && window.google.accounts && window.google.accounts.id) {
    window.google.accounts.id.disableAutoSelect?.()
  }
  window.location.hash = '#/'
}
