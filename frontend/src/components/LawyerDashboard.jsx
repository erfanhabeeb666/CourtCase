import React, { useState } from 'react'
import { uploadDocumentForRole } from '../api/client'
import SidebarLayout from './SidebarLayout'

export default function LawyerDashboard({ token }) {
  const [caseId, setCaseId] = useState('')
  const [file, setFile] = useState(null)
  const [note, setNote] = useState('')
  const [ok, setOk] = useState(false)
  const [error, setError] = useState('')

  const onSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setOk(false)
    try {
      await uploadDocumentForRole(token, 'LAWYER', { caseId, file, note })
      setOk(true)
      setCaseId(''); setFile(null); setNote('')
    } catch (e) {
      setError(e.message)
    }
  }

  const sections = [
    {
      key: 'upload',
      label: 'Upload Document',
      icon: '📤',
      render: () => (
        <section className="card">
          <h2>Upload Document (Lawyer)</h2>
          <form onSubmit={onSubmit} className="form-grid">
            <label>
              Case ID
              <input type="number" value={caseId} onChange={(e) => setCaseId(e.target.value)} required />
            </label>
            <label>
              File
              <input type="file" onChange={(e) => setFile(e.target.files?.[0])} required />
            </label>
            <label>
              Note
              <input value={note} onChange={(e) => setNote(e.target.value)} placeholder="optional" />
            </label>
            <button type="submit" disabled={!token || !caseId || !file}>Upload</button>
          </form>
          {ok && <div className="success">Uploaded</div>}
          {error && <div className="error">{error}</div>}
        </section>
      )
    }
  ]

  return (
    <SidebarLayout sections={sections} defaultKey="upload" persistKey="sidebar:lawyer" />
  )
}
