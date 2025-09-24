import React, { useEffect, useState } from 'react'
import { clientFileCase, clientMyCases, clientListLawyers } from '../api/client'
import SidebarLayout from './SidebarLayout'

export default function ClientDashboard({ token }) {
  const [title, setTitle] = useState('')
  const [type, setType] = useState('')
  const [description, setDescription] = useState('')
  const [clientLawyerId, setClientLawyerId] = useState('')
  const [fileMsg, setFileMsg] = useState('')
  const [fileErr, setFileErr] = useState('')
  const [lawyers, setLawyers] = useState([])
  const [lawyersErr, setLawyersErr] = useState('')

  const [cases, setCases] = useState([])
  const [listErr, setListErr] = useState('')

  const submitFile = async (e) => {
    e.preventDefault()
    setFileErr(''); setFileMsg('')
    try {
      const payload = { title, type: type || null, description, clientLawyerId }
      const msg = await clientFileCase(token, payload)
      setFileMsg(msg)
      setTitle(''); setType(''); setDescription(''); setClientLawyerId('')
    } catch (e) { setFileErr(e.message) }
  }

  const loadMyCases = async () => {
    setListErr('')
    try { setCases(await clientMyCases(token)) } catch (e) { setListErr(e.message) }
  }

  const sections = [
    {
      key: 'fileCase',
      label: 'File Case',
      icon: '🗂️',
      render: () => (
        <section className="card">
          <h2>File Case</h2>
          <form onSubmit={submitFile} className="form-grid">
            <label>Title<input value={title} onChange={(e) => setTitle(e.target.value)} required /></label>
            <label>Type<input value={type} onChange={(e) => setType(e.target.value)} placeholder="e.g., CIVIL, CRIMINAL" /></label>
            <label>Description<input value={description} onChange={(e) => setDescription(e.target.value)} /></label>
            <label>
              Select Your Lawyer (optional)
              <select value={clientLawyerId} onChange={(e) => setClientLawyerId(e.target.value)} onFocus={async () => {
                if (lawyers.length === 0) {
                  setLawyersErr('')
                  try { setLawyers(await clientListLawyers(token)) } catch (e) { setLawyersErr(e.message) }
                }
              }}>
                <option value="">-- No lawyer selected --</option>
                {lawyers.map(l => (
                  <option key={l.id} value={l.id}>{l.name || l.email} ({l.status})</option>
                ))}
              </select>
            </label>
            {lawyersErr && <div className="error">{lawyersErr}</div>}
            <button type="submit" disabled={!token}>Submit</button>
          </form>
          {fileMsg && <div className="success">{fileMsg}</div>}
          {fileErr && <div className="error">{fileErr}</div>}
        </section>
      )
    },
    {
      key: 'myCases',
      label: 'My Cases',
      icon: '📄',
      render: () => (
        <section className="card">
          <h2>My Cases</h2>
          <button onClick={loadMyCases}>Load</button>
          {listErr && <div className="error">{listErr}</div>}
          {cases.length > 0 && (
            <ul>
              {cases.map(c => (
                <li key={c.id}><strong>{c.title}</strong> — Status: {c.status} — Next: {c.nextHearingDate || '-'}</li>
              ))}
            </ul>
          )}
        </section>
      )
    }
  ]

  return (
    <SidebarLayout sections={sections} defaultKey="fileCase" persistKey="sidebar:client" />
  )
}
