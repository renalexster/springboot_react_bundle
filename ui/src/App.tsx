import React, { useEffect } from 'react'
import { useHashLocation } from './hooks/useHashLocation'
import Login from './pages/Login'
import Welcome from './pages/Welcome'

export default function App() {
  const path = useHashLocation()

  useEffect(() => {
    if (!window.location.hash) {
      window.location.hash = '#/'
    }
  }, [])

  if (path.startsWith('/welcome')) return <Welcome />
  return <Login />
}
