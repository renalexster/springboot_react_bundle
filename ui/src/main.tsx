import React from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'
import './styles/base.css'

const el = document.getElementById('root') as HTMLElement
const enableStrict = import.meta.env.VITE_ENABLE_STRICT_MODE === 'true'
createRoot(el).render(
    enableStrict ? (
        <React.StrictMode>
            <App />
        </React.StrictMode>
    ) : (
        <App />
    )
)