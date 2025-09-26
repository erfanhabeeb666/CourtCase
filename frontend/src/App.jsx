import React, { useEffect, useMemo, useState } from 'react'
import TodaysHearings from './components/TodaysHearings.jsx'
import JudgeMyCases from './components/JudgeMyCases.jsx'
import Login from './components/Login.jsx'
import { me } from './api/client.js'
import AdminDashboard from './components/AdminDashboard.jsx'
import LawyerDashboard from './components/LawyerDashboard.jsx'
import ClientDashboard from './components/ClientDashboard.jsx'
import SidebarLayout from './components/SidebarLayout.jsx'
import Landing from './components/Landing.jsx'
import TopNav from './components/TopNav.jsx'

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
  // Landing/auth flow
  const [showAuth, setShowAuth] = useState(false)
  const [authMode, setAuthMode] = useState('login') // 'login' | 'register'

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
    if (!token) {
      if (!showAuth) {
        return (
          <Landing
            onGetStarted={() => { setAuthMode('register'); setShowAuth(true) }}
            onSignIn={() => { setAuthMode('login'); setShowAuth(true) }}
          />
        )
      }
      return <Login onLoginSuccess={setToken} initialMode={authMode} onBack={() => setShowAuth(false)} />
    }
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
  }, [token, user, error, showAuth, authMode])

  return (
    <div className="container">
      <header>
        <TopNav
          isAuthed={!!token}
          onLogout={() => setToken('')}
          theme={theme}
          onToggleTheme={() => setTheme(prev => (prev === 'light' ? 'dark' : 'light'))}
          onBack={() => {
            if (!token && showAuth) {
              setShowAuth(false)
              return
            }
            if (window && window.history) window.history.back()
          }}
        />
      </header>
      {content}
    </div>
  )
}

