import React, { useEffect } from 'react'
import { setToken } from '../services/auth'

const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID

export default function Login() {
  useEffect(() => {
    if (!clientId) {
      console.error('VITE_GOOGLE_CLIENT_ID is not set')
      return
    }

    const handleCredentialResponse = (response: { credential?: string }) => {
      const idToken = response.credential
      if (!idToken) return
      setToken(idToken)
      window.location.hash = '#/welcome'
    }

    const init = () => {
      if (!window.google || !window.google.accounts || !window.google.accounts.id) {
        console.error('Google Identity Services not loaded')
        return
      }
      window.google.accounts.id.initialize({
        client_id: clientId,
        callback: handleCredentialResponse,
      })
      const btn = document.getElementById('google-signin-btn')
      if (btn) {
        window.google.accounts.id.renderButton(btn, {
          theme: 'outline',
          size: 'large',
          width: 320,
        })
      }
      // Optionally prompt
      // window.google.accounts.id.prompt?.()
    }

    // Load script dynamically if not present
    const scriptId = 'google-identity-services'
    if (!document.getElementById(scriptId)) {
      const script = document.createElement('script')
      script.src = 'https://accounts.google.com/gsi/client'
      script.async = true
      script.defer = true
      script.id = scriptId
      script.onload = init
      document.head.appendChild(script)
    } else {
      init()
    }
  }, [])

  return (
    <div className="center-full">
      <div className="stack" style={{ alignItems: 'center' }}>
        <h1>Sign in</h1>
        <div id="google-signin-btn" />
      </div>
    </div>
  )
}
