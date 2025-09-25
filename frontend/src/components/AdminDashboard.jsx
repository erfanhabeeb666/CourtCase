import React, { useState } from 'react'
import { adminAddJudge, adminAssignCase, adminListUsers, adminUpdateUserStatus, adminRegisterClient, adminRegisterLawyer, adminListCases, adminListJudges, adminListLawyers } from '../api/client'
import SidebarLayout from './SidebarLayout'

export default function AdminDashboard({ token }) {
  // Add judge form
  const [jName, setJName] = useState('')
  const [jEmail, setJEmail] = useState('')
  const [jPassword, setJPassword] = useState('')
  const [jLegalId, setJLegalId] = useState('')
  const [jMsg, setJMsg] = useState('')
  const [jErr, setJErr] = useState('')

  // Assign case form
  const [caseId, setCaseId] = useState('')
  const [opposingLawyerId, setOpposingLawyerId] = useState('')
  const [judgeId, setJudgeId] = useState('')
  const [nextHearingDate, setNextHearingDate] = useState('')
  const [assignResult, setAssignResult] = useState(null)
  const [assignErr, setAssignErr] = useState('')
  const [cases, setCases] = useState([])
  const [judges, setJudges] = useState([])
  const [lawyers, setLawyers] = useState([])
  const [listsErr, setListsErr] = useState('')

  // User management
  const [roleFilter, setRoleFilter] = useState('') // ADMIN, JUDGE, LAWYER, CLIENT or empty for all
  const [users, setUsers] = useState([])
  const [usersErr, setUsersErr] = useState('')
  const [statusBusyId, setStatusBusyId] = useState(null)

  // Register client/lawyer
  const [cName, setCName] = useState('')
  const [cEmail, setCEmail] = useState('')
  const [cPassword, setCPassword] = useState('')
  const [cLegalId, setCLegalId] = useState('')
  const [cMsg, setCMsg] = useState('')
  const [cErr, setCErr] = useState('')

  const submitAddJudge = async (e) => {
    e.preventDefault()
    setJErr(''); setJMsg('')
    try {
      const msg = await adminAddJudge(token, { name: jName, email: jEmail, password: jPassword, legalIdentity: jLegalId })
      setJMsg(msg)
      setJName(''); setJEmail(''); setJPassword(''); setJLegalId('')
    } catch (e) {
      setJErr(e.message)
    }
  }

  const loadUsers = async () => {
    setUsersErr('')
    try {
      const list = await adminListUsers(token, roleFilter || undefined)
      setUsers(list)
    } catch (e) {
      setUsersErr(e.message)
    }
  }

  const toggleStatus = async (u) => {
    setStatusBusyId(u.id)
    try {
      const newStatus = u.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
      const updated = await adminUpdateUserStatus(token, u.id, newStatus)
      setUsers((prev) => prev.map(x => (x.id === updated.id ? updated : x)))
    } catch (e) {
      alert(e.message)
    } finally {
      setStatusBusyId(null)
    }
  }

  const submitRegisterClient = async (e) => {
    e.preventDefault()
    setCErr(''); setCMsg('')
    try {
      // For client, legalIdentity is optional in your DTO
      const msg = await adminRegisterClient(token, { name: cName, email: cEmail, password: cPassword, legalIdentity: cLegalId || undefined })
      setCMsg(msg)
      setCName(''); setCEmail(''); setCPassword(''); setCLegalId('')
    } catch (e) { setCErr(e.message) }
  }

  const submitRegisterLawyer = async (e) => {
    e.preventDefault()
    setCErr(''); setCMsg('')
    try {
      const msg = await adminRegisterLawyer(token, { name: cName, email: cEmail, password: cPassword, legalIdentity: cLegalId })
      setCMsg(msg)
      setCName(''); setCEmail(''); setCPassword(''); setCLegalId('')
    } catch (e) { setCErr(e.message) }
  }

  const submitAssignCase = async (e) => {
    e.preventDefault()
    setAssignErr(''); setAssignResult(null)
    try {
      const payload = {
        opposingLawyerId: Number(opposingLawyerId),
        judgeId: Number(judgeId),
        nextHearingDate: nextHearingDate || null
      }
      const result = await adminAssignCase(token, Number(caseId), payload)
      setAssignResult(result)
    } catch (e) {
      setAssignErr(e.message)
    }
  }

  const ensureListsLoaded = async () => {
    if (!token) return
    setListsErr('')
    try {
      if (cases.length === 0) setCases(await adminListCases(token))
      if (judges.length === 0) setJudges(await adminListJudges(token))
      if (lawyers.length === 0) setLawyers(await adminListLawyers(token))
    } catch (e) {
      setListsErr(e.message)
    }
  }

  const sections = [
    {
      key: 'addJudge',
      label: 'Add Judge',
      icon: '⚖️',
      render: () => (
        <section className="card">
          <h2>Add Judge</h2>
          <form onSubmit={submitAddJudge} className="form-grid">
            <label>Name<input value={jName} onChange={(e) => setJName(e.target.value)} required /></label>
            <label>Email<input type="email" value={jEmail} onChange={(e) => setJEmail(e.target.value)} required /></label>
            <label>Password<input type="password" value={jPassword} onChange={(e) => setJPassword(e.target.value)} required /></label>
            <label>Legal Identity<input value={jLegalId} onChange={(e) => setJLegalId(e.target.value)} /></label>
            <button type="submit" disabled={!token}>Create Judge</button>
          </form>
          {jMsg && <div className="success">{jMsg}</div>}
          {jErr && <div className="error">{jErr}</div>}
        </section>
      )
    },
    {
      key: 'assignCase',
      label: 'Assign Case',
      icon: '📌',
      render: () => (
        <section className="card">
          <h2>Assign Case</h2>
          <form onSubmit={submitAssignCase} className="form-grid" onFocusCapture={ensureListsLoaded}>
            <label>
              Select Case
              <select value={caseId} onChange={(e) => setCaseId(e.target.value)} required>
                <option value="">-- Select a case --</option>
                {cases.map(c => (
                  <option key={c.id} value={c.id}>{c.title} (Status: {c.status})</option>
                ))}
              </select>
            </label>
            <label>
              Select Opposing Lawyer
              <select value={opposingLawyerId} onChange={(e) => setOpposingLawyerId(e.target.value)} required>
                <option value="">-- Select a lawyer --</option>
                {lawyers.map(l => (
                  <option key={l.id} value={l.id}>{l.name || l.email} ({l.status})</option>
                ))}
              </select>
            </label>
            <label>
              Select Judge
              <select value={judgeId} onChange={(e) => setJudgeId(e.target.value)} required>
                <option value="">-- Select a judge --</option>
                {judges.map(j => (
                  <option key={j.id} value={j.id}>{j.name || j.email} ({j.status})</option>
                ))}
              </select>
            </label>
            <label>Next Hearing Date (YYYY-MM-DD)<input value={nextHearingDate} onChange={(e) => setNextHearingDate(e.target.value)} /></label>
            <button type="submit" disabled={!token || !caseId}>Assign</button>
          </form>
          {listsErr && <div className="error">{listsErr}</div>}
          {assignErr && <div className="error">{assignErr}</div>}
          {assignResult && <pre className="result">{JSON.stringify(assignResult, null, 2)}</pre>}
        </section>
      )
    },
    {
      key: 'userMgmt',
      label: 'User Management',
      icon: '👥',
      render: () => (
        <section className="card">
          <h2>User Management</h2>
          <div className="form-grid">
            <label>
              Role Filter
              <select value={roleFilter} onChange={(e) => setRoleFilter(e.target.value)}>
                <option value="">All</option>
                <option value="ADMIN">ADMIN</option>
                <option value="JUDGE">JUDGE</option>
                <option value="LAWYER">LAWYER</option>
                <option value="CLIENT">CLIENT</option>
              </select>
            </label>
            <button onClick={loadUsers} disabled={!token}>Load Users</button>
          </div>
          {usersErr && <div className="error">{usersErr}</div>}
          {users.length > 0 && (
            <ul>
              {users.map(u => (
                <li key={u.id}>
                  <strong>{u.name || '(no name)'} </strong> — {u.email} — {u.userType} — Status: {u.status}
                  <div style={{ marginTop: 6 }}>
                    <button onClick={() => toggleStatus(u)} disabled={statusBusyId === u.id}>
                      {u.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </section>
      )
    },
    {
      key: 'register',
      label: 'Register Client/Lawyer',
      icon: '📝',
      render: () => (
        <section className="card">
          <h2>Register Client / Lawyer</h2>
          <form className="form-grid" onSubmit={submitRegisterClient}>
            <label>Name<input value={cName} onChange={(e) => setCName(e.target.value)} required /></label>
            <label>Email<input type="email" value={cEmail} onChange={(e) => setCEmail(e.target.value)} required /></label>
            <label>Password<input type="password" value={cPassword} onChange={(e) => setCPassword(e.target.value)} required /></label>
            <label>Legal Identity<input value={cLegalId} onChange={(e) => setCLegalId(e.target.value)} placeholder="optional for Client, required for Lawyer" /></label>
            <div className="grid-2">
              <button type="submit" disabled={!token}>Register Client</button>
              <button type="button" onClick={submitRegisterLawyer} disabled={!token || !cLegalId}>Register Lawyer</button>
            </div>
          </form>
          {cMsg && <div className="success">{cMsg}</div>}
          {cErr && <div className="error">{cErr}</div>}
        </section>
      )
    }
  ]

  return (
    <SidebarLayout sections={sections} defaultKey="addJudge" persistKey="sidebar:admin" />
  )
}
