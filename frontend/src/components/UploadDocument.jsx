import React, { useState } from 'react'
import { uploadDocument } from '../api/client'

export default function UploadDocument({ token }) {
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
      await uploadDocument(token, { caseId, file, note })
      setOk(true)
    } catch (e) {
      setError(e.message)
    }
  }

  return (
    <section className="card">
      <h2>Upload Document</h2>
      <form onSubmit={onSubmit} className="form-grid">
        <label>
          Case ID
          <input type="text" value={caseId} onChange={(e) => setCaseId(e.target.value)} />
        </label>
        <label>
          File
          <input type="file" accept="application/pdf" onChange={(e) => setFile(e.target.files?.[0])} />
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
