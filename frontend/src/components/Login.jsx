import React, { useState } from 'react'
import { authenticate, me, registerClientPublic } from '../api/client'

export default function Login({ onLoginSuccess, initialMode = 'login', onBack }) {
  const [mode, setMode] = useState(initialMode) // 'login' | 'register'
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [legalIdentity, setLegalIdentity] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      if (mode === 'register') {
        // Register client publicly
        await registerClientPublic({ name, email, password, legalIdentity })
        // Auto-login after successful registration
        const { token } = await authenticate(email, password)
        if (!token) throw new Error('No token returned')
        await me(token)
        onLoginSuccess(token)
      } else {
        const { token } = await authenticate(email, password)
        if (!token) throw new Error('No token returned')
        await me(token)
        onLoginSuccess(token)
      }
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="card">
      <h2>{mode === 'register' ? 'Create Account (Client)' : 'Login'}</h2>
      <form onSubmit={submit} className="form-grid">
        {mode === 'register' && (
          <label>
            Name
            <input value={name} onChange={(e) => setName(e.target.value)} required={mode === 'register'} />
          </label>
        )}
        <label>
          Email
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>
        <label>
          Password
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </label>
        {mode === 'register' && (
          <label>
            Legal Identity
            <input value={legalIdentity} onChange={(e) => setLegalIdentity(e.target.value)} placeholder="e.g., Government ID" required />
          </label>
        )}
        <button type="submit" disabled={loading}>
          {loading ? (mode === 'register' ? 'Creating account...' : 'Signing in...') : (mode === 'register' ? 'Create Account' : 'Sign In')}
        </button>
      </form>
      <div style={{ marginTop: 10 }}>
        {mode === 'register' ? (
          <button type="button" onClick={() => setMode('login')}>Already have an account? Sign In</button>
        ) : (
          <button type="button" onClick={() => setMode('register')}>New here? Create a Client Account</button>
        )}
      </div>
      {error && <div className="error" style={{ marginTop: 8 }}>{error}</div>}
    </section>
  )
}
