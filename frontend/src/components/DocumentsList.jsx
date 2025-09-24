import React, { useState } from 'react'
import { getDocuments, downloadDocument } from '../api/client'

export default function DocumentsList({ token, caseId }) {
  const [items, setItems] = useState([])
  const [error, setError] = useState('')

  const load = async () => {
    setError('')
    try {
      const data = await getDocuments(token, caseId)
      setItems(data)
    } catch (e) {
      setError(e.message)
    }
  }

  return (
    <section className="card">
      <h2>Documents</h2>
      <button onClick={load} disabled={!caseId}>Load Documents</button>
      {error && <div className="error">{error}</div>}
      <ul>
        {items.map(d => (
          <li key={d.id}>
            <strong>{d.fileName}</strong> — Uploaded: {d.uploadedAt}
            <div>Note: {d.note || '-'}</div>
            <div style={{ marginTop: 6 }}>
              <button onClick={async () => {
                try {
                  const { blob, filename } = await downloadDocument(token, d.id)
                  const url = URL.createObjectURL(blob)
                  const a = document.createElement('a')
                  a.href = url
                  a.download = filename
                  document.body.appendChild(a)
                  a.click()
                  a.remove()
                  URL.revokeObjectURL(url)
                } catch (e) {
                  setError(e.message)
                }
              }} disabled={!token}>Download</button>
            </div>
          </li>
        ))}
      </ul>
    </section>
  )
}
