import React, { useEffect, useState } from 'react'
import { getToken, verifyToken, logout } from '../services/auth'

export default function Welcome() {
  const [name, setName] = useState<string>('')
  const [error, setError] = useState<string>('')

  useEffect(() => {
    const token = getToken()
    if (!token) {
      window.location.hash = '#/'
      return
    }

    verifyToken(token)
      .then((data) => setName(data.name || ''))
      .catch((e: unknown) => {
        console.error(e)
        setError('Token validation failed. Please sign in again.')
      })
  }, [])

  return (
    <div style={{ padding: 24 }}>
      <h1>Welcome</h1>
      {error ? (
        <p className="error">{error}</p>
      ) : name ? (
        <p>Hello, {name}!</p>
      ) : (
        <p>Loading your profile...</p>
      )}
      <button onClick={logout} className="mt-16">Sign out</button>
    </div>
  )
}
