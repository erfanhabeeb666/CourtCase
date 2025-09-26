import React from 'react'

export default function TopNav({ isAuthed, onLogout, theme, onToggleTheme, onBack }) {
  const handleBack = () => {
    if (typeof onBack === 'function') return onBack()
    if (window && window.history) window.history.back()
  }
  return (
    <nav className="topnav">
      <div className="topnav__left">
        <button className="secondary btn-inline" onClick={handleBack} title="Back" aria-label="Go Back">←</button>
        <span className="brand">Court Case Management</span>
      </div>
      <div className="topnav__right">
        <button className="secondary" onClick={onToggleTheme}>
          {theme === 'light' ? 'Dark Mode' : 'Light Mode'}
        </button>
        {isAuthed && (
          <button className="danger" onClick={onLogout} title="Logout">
            Logout
          </button>
        )}
      </div>
    </nav>
  )
}
