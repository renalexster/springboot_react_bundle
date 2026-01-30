import { useMemo, useSyncExternalStore } from 'react'

export function useHashLocation(): string {
  const subscribe = (onChange: () => void) => {
    window.addEventListener('hashchange', onChange)
    return () => window.removeEventListener('hashchange', onChange)
  }
  const getSnapshot = () => window.location.hash || '#/'
  const hash = useSyncExternalStore(subscribe, getSnapshot, getSnapshot)
  return useMemo(() => hash.replace(/^#/, ''), [hash])
}
