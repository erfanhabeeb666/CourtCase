import React, { useEffect, useMemo, useState } from 'react'
import TodaysHearings from './components/TodaysHearings.jsx'
import JudgeMyCases from './components/JudgeMyCases.jsx'
import Login from './components/Login.jsx'
import { me } from './api/client.js'
import AdminDashboard from './components/AdminDashboard.jsx'
import LawyerDashboard from './components/LawyerDashboard.jsx'
import ClientDashboard from './components/ClientDashboard.jsx'
import SidebarLayout from './components/SidebarLayout.jsx'

function JudgeDashboard({ token }) {
  const sections = [
    {
      key: 'today',
      label: "Today's Hearings",
      icon: '🕒',
      render: () => (<TodaysHearings token={token} />)
    },
    {
      key: 'mycases',
      label: 'My Cases',
      icon: '📁',
      render: () => (<JudgeMyCases token={token} />)
    }
  ]
  return (
    <SidebarLayout sections={sections} defaultKey="today" persistKey="sidebar:judge" />
  )
}

export default function App() {
  const [token, setToken] = useState(() => localStorage.getItem('token') || '')
  const [user, setUser] = useState(null)
  const [error, setError] = useState('')
  const [theme, setTheme] = useState(() => localStorage.getItem('theme') || 'dark')

  useEffect(() => {
    if (token) {
      localStorage.setItem('token', token)
      ;(async () => {
        try {
          const u = await me(token)
          setUser(u)
        } catch (e) {
          setError(e.message)
          setUser(null)
        }
      })()
    } else {
      localStorage.removeItem('token')
      setUser(null)
    }
  }, [token])

  // Apply theme to <html> via data-theme attribute and persist
  useEffect(() => {
    if (theme === 'light') {
      document.documentElement.setAttribute('data-theme', 'light')
    } else {
      document.documentElement.removeAttribute('data-theme')
    }
    localStorage.setItem('theme', theme)
  }, [theme])

  const content = useMemo(() => {
    if (!token) return <Login onLoginSuccess={setToken} />
    if (!user) return <section className="card"><h2>Loading profile...</h2>{error && <div className="error">{error}</div>}</section>
    switch (user.userType) {
      case 'JUDGE':
        return <JudgeDashboard token={token} />
      case 'ADMIN':
        return <AdminDashboard token={token} />
      case 'LAWYER':
        return <LawyerDashboard token={token} />
      case 'CLIENT':
        return <ClientDashboard token={token} />
      default:
        return <section className="card"><h2>Unknown role</h2></section>
    }
  }, [token, user, error])

  return (
    <div className="container">
      <header>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 12, flexWrap: 'wrap' }}>
          <h1 style={{ marginBottom: 0 }}>Court Case Management</h1>
          <div style={{ minWidth: 180, display: 'flex', gap: 8 }}>
            <button onClick={() => setTheme(prev => (prev === 'light' ? 'dark' : 'light'))}>
              {theme === 'light' ? 'Switch to Dark' : 'Switch to Light'} Theme
            </button>
          </div>
        </div>
      </header>
      {content}
      {token && (
        <section className="card">
          <button onClick={() => setToken('')}>Logout</button>
        </section>
      )}
      <footer>
        <small>Set VITE_API_BASE in .env to your backend base URL (e.g., http://localhost:8080)</small>
      </footer>
    </div>
  )
}
