// Centralized fetch wrapper that injects an x-traceId header with a UUID for every request
// and preserves all provided options.

export const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

function generateUuid(): string {
  // Prefer Web Crypto random UUID when available
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto && typeof crypto.randomUUID === 'function') {
    try { return crypto.randomUUID() } catch {}
  }
  // RFC4122 v4-ish fallback (not cryptographically secure, but fine for trace IDs)
  // https://stackoverflow.com/a/2117523
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

export type ApiFetch = (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>

export const apiFetch: ApiFetch = (input, init = {}) => {
  const traceId = generateUuid()

  // Normalize existing headers to a Headers instance for easier merging
  const headers = new Headers(init.headers || {})
  headers.set('x-traceId', traceId)

  const mergedInit: RequestInit = {
    ...init,
    headers,
  }

  return fetch(input, mergedInit)
}
