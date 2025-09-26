import React, { useState, useMemo, useEffect } from 'react'

/**
 * SidebarLayout renders a vertical sidebar with buttons and a main content area.
 *
 * Props:
 * - sections: Array<{ key: string, label: string, icon?: React.ReactNode, render: () => React.ReactNode }>
 *   Provide unique keys. The first section will be active by default unless defaultKey is provided.
 * - defaultKey?: string
 * - persistKey?: string  // when provided, active tab will persist in localStorage under this key
 * - sidebarWidth?: number (pixels)
 */
export default function SidebarLayout({ sections = [], defaultKey, persistKey, sidebarWidth = 220 }) {
  const computeInitialKey = () => {
    if (persistKey) {
      const saved = localStorage.getItem(persistKey)
      if (saved && sections.some(s => s.key === saved)) return saved
    }
    if (defaultKey && sections.some(s => s.key === defaultKey)) return defaultKey
    return sections[0]?.key
  }

  const [activeKey, setActiveKey] = useState(computeInitialKey)
  const [hoveredKey, setHoveredKey] = useState(null)

  useEffect(() => {
    if (!persistKey) return
    if (activeKey) localStorage.setItem(persistKey, String(activeKey))
  }, [activeKey, persistKey])

  const ActiveContent = useMemo(() => sections.find(s => s.key === activeKey)?.render, [sections, activeKey])

  return (
    <div style={{ display: 'flex', gap: 16, alignItems: 'flex-start' }}>
      <aside
        className="card"
        style={{
          width: sidebarWidth,
          padding: 0,
          position: 'sticky',
          top: 16,
          alignSelf: 'flex-start'
        }}
      >
        <div style={{ borderBottom: '1px solid var(--muted)', padding: '12px 14px' }}>
          <strong>Actions</strong>
        </div>
        <nav style={{ display: 'flex', flexDirection: 'column' }}>
          {sections.map(s => {
            const isActive = s.key === activeKey
            const isHovered = hoveredKey === s.key
            const bg = isActive
              ? 'var(--accent-bg, #2563eb)'
              : isHovered
                ? 'rgba(0,0,0,0.06)'
                : 'transparent'
            const color = isActive ? 'var(--accent-fg, #fff)' : 'inherit'
            return (
              <button
                key={s.key}
                onClick={() => setActiveKey(s.key)}
                onMouseEnter={() => setHoveredKey(s.key)}
                onMouseLeave={() => setHoveredKey(null)}
                style={{
                  textAlign: 'left',
                  display: 'flex',
                  alignItems: 'center',
                  gap: 10,
                  borderRadius: 0,
                  border: 'none',
                  borderTop: '1px solid var(--muted)'
                    ,
                  background: bg,
                  color,
                  padding: '10px 14px',
                  cursor: 'pointer',
                  transition: 'background 120ms ease'
                }}
              >
                {s.icon && <span aria-hidden>{s.icon}</span>}
                <span>{s.label}</span>
              </button>
            )
          })}
        </nav>
      </aside>
      <main style={{ flex: 1, minWidth: 0 }}>
        {ActiveContent ? ActiveContent() : (
          <section className="card"><h2>No section selected</h2></section>
        )}
      </main>
    </div>
  )
}

