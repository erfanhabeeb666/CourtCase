import React, { useEffect, useState } from 'react'
import { uploadDocumentForRole, lawyerMyCases, lawyerGetCase, lawyerGetHearings, fetchDocumentBlob } from '../api/client'
import SidebarLayout from './SidebarLayout'

export default function LawyerDashboard({ token }) {
  const [caseId, setCaseId] = useState('')
  const [file, setFile] = useState(null)
  const [note, setNote] = useState('')
  const [ok, setOk] = useState(false)
  const [error, setError] = useState('')

  // My Cases state
  const [myCases, setMyCases] = useState([])
  const [myCasesLoading, setMyCasesLoading] = useState(false)
  const [myCasesErr, setMyCasesErr] = useState('')
  const [openCaseId, setOpenCaseId] = useState(null)
  const [detailsMap, setDetailsMap] = useState({}) // { [caseId]: { loading, error, hearings } }

  // Search Case state
  const [searchId, setSearchId] = useState('')
  const [searchCase, setSearchCase] = useState(null)
  const [searchHearings, setSearchHearings] = useState([])
  const [searchErr, setSearchErr] = useState('')
  const [viewer, setViewer] = useState({ open: false, url: '', name: '' })

  // Per-case document upload state
  const [uploadMap, setUploadMap] = useState({}) // { [caseId]: { file, note, uploading, error, ok } }

  const setUploadField = (caseId, field, value) => {
    setUploadMap(prev => ({
      ...prev,
      [caseId]: {
        ...(prev[caseId] || { file: null, note: '', uploading: false, error: '', ok: false }),
        [field]: value
      }
    }))
  }

  const submitUpload = async (cid) => {
    const state = uploadMap[cid] || { file: null, note: '' }
    if (!state.file) {
      setUploadField(cid, 'error', 'Please choose a file')
      return
    }
    setUploadField(cid, 'error', '')
    setUploadField(cid, 'ok', false)
    setUploadField(cid, 'uploading', true)
    try {
      await uploadDocumentForRole(token, 'LAWYER', { caseId: cid, file: state.file, note: state.note })
      setUploadField(cid, 'ok', true)
      setUploadField(cid, 'file', null)
      setUploadField(cid, 'note', '')
    } catch (e) {
      setUploadField(cid, 'error', String(e.message || e))
    } finally {
      setUploadField(cid, 'uploading', false)
    }
  }

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

  const loadMyCases = async () => {
    if (!token) return
    setMyCasesLoading(true); setMyCasesErr('')
    try {
      const data = await lawyerMyCases(token)
      setMyCases(data)
    } catch (e) {
      setMyCasesErr(String(e.message || e))
    } finally {
      setMyCasesLoading(false)
    }
  }

  useEffect(() => {
    loadMyCases()
  }, [token])

  const toggleDetails = async (cid) => {
    if (openCaseId === cid) { setOpenCaseId(null); return }
    setOpenCaseId(cid)
    if (!detailsMap[cid]) {
      setDetailsMap(prev => ({ ...prev, [cid]: { loading: true, error: '', hearings: [] } }))
      try {
        const hearings = await lawyerGetHearings(token, cid)
        setDetailsMap(prev => ({ ...prev, [cid]: { loading: false, error: '', hearings } }))
      } catch (e) {
        setDetailsMap(prev => ({ ...prev, [cid]: { loading: false, error: String(e.message || e), hearings: [] } }))
      }
    }
  }

  const doSearch = async (e) => {
    e?.preventDefault?.()
    setSearchErr(''); setSearchCase(null); setSearchHearings([])
    if (!searchId) { setSearchErr('Enter case ID'); return }
    try {
      const c = await lawyerGetCase(token, Number(searchId))
      setSearchCase(c)
      const hs = await lawyerGetHearings(token, Number(searchId))
      setSearchHearings(hs)
    } catch (e) {
      setSearchErr(String(e.message || e))
    }
  }

  const openPdfViewer = async (doc) => {
    try {
      const blob = await fetchDocumentBlob(token, doc.id)
      const url = URL.createObjectURL(blob)
      setViewer({ open: true, url, name: doc.fileName || `document-${doc.id}.pdf` })
    } catch (e) {
      setError(String(e.message || e))
    }
  }

  const closePdfViewer = () => {
    if (viewer.url) URL.revokeObjectURL(viewer.url)
    setViewer({ open: false, url: '', name: '' })
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
    },
    {
      key: 'mycases',
      label: 'My Cases',
      icon: '📁',
      render: () => (
        <section className="card">
          <h2>My Cases</h2>
          <button onClick={loadMyCases} disabled={!token || myCasesLoading}>{myCasesLoading ? 'Loading...' : 'Reload'}</button>
          {myCasesErr && <div className="error" style={{ marginTop: 8 }}>{myCasesErr}</div>}
          <div style={{ margin: '6px 0' }}>
            <small>Loaded {myCases.length} cases.</small>
          </div>
          {myCases.length > 0 ? (
            <ul>
              {myCases.map(c => (
                <li key={c.id} style={{ marginBottom: 12 }}>
                  <div><strong>Case:</strong> {c.title} — <strong>Status:</strong> {c.status}</div>
                  <div><strong>Next Hearing:</strong> {c.nextHearingDate || '-'}</div>
                  {(c.verdict || c.status === 'CLOSED') && (
                    <div><strong>Verdict:</strong> {c.verdict || '-'}{c.verdictDate ? ` — on ${c.verdictDate}` : ''}</div>
                  )}
                  <button style={{ marginTop: 6 }} onClick={() => toggleDetails(c.id)}>
                    {openCaseId === c.id ? 'Hide Hearings' : 'View Hearings'}
                  </button>
                  {openCaseId === c.id && (
                    <div className="card" style={{ marginTop: 8 }}>
                      {detailsMap[c.id]?.loading && <div>Loading hearings...</div>}
                      {detailsMap[c.id]?.error && <div className="error">{detailsMap[c.id]?.error}</div>}
                      {!!detailsMap[c.id] && !detailsMap[c.id].loading && (
                        <>
                          <ul>
                            {detailsMap[c.id].hearings.map(h => (
                              <li key={h.id}><strong>{h.hearingDate}</strong> — {h.judgeSummary || '-'}{h.status ? ` — ${h.status}` : ''}</li>
                            ))}
                          </ul>

                          <div style={{ borderTop: '1px solid var(--muted)', marginTop: 12, paddingTop: 12 }}>
                            <h4 style={{ marginTop: 0 }}>Upload Document to this Case</h4>
                            <div className="form-grid">
                              <label>
                                File
                                <input
                                  type="file"
                                  accept="application/pdf"
                                  onChange={(e) => setUploadField(c.id, 'file', e.target.files?.[0] || null)}
                                />
                              </label>
                              <label>
                                Note
                                <input
                                  value={uploadMap[c.id]?.note || ''}
                                  onChange={(e) => setUploadField(c.id, 'note', e.target.value)}
                                  placeholder="optional"
                                />
                              </label>
                              <button onClick={() => submitUpload(c.id)} disabled={uploadMap[c.id]?.uploading}>
                                {uploadMap[c.id]?.uploading ? 'Uploading...' : 'Upload'}
                              </button>
                            </div>
                            {uploadMap[c.id]?.ok && <div className="success">Uploaded</div>}
                            {uploadMap[c.id]?.error && <div className="error">{uploadMap[c.id]?.error}</div>}
                          </div>
                        </>
                      )}
                    </div>
                  )}
                </li>
              ))}
            </ul>
          ) : (
            <p>No cases found.</p>
          )}
        </section>
      )
    },
    {
      key: 'search',
      label: 'Search Case',
      icon: '🔎',
      render: () => (
        <section className="card">
          <h2>Search Case by ID</h2>
          <form className="form-grid" onSubmit={doSearch}>
            <label>
              Case ID
              <input type="number" value={searchId} onChange={(e) => setSearchId(e.target.value)} />
            </label>
            <button type="submit" disabled={!token || !searchId}>Search</button>
          </form>
          {searchErr && <div className="error">{searchErr}</div>}
          {searchCase && (
            <div className="card" style={{ marginTop: 8 }}>
              <div><strong>Case:</strong> {searchCase.title} — <strong>Status:</strong> {searchCase.status}</div>
              <div><strong>Next Hearing:</strong> {searchCase.nextHearingDate || '-'}</div>
              <h4 style={{ marginTop: 8 }}>Hearings</h4>
              <ul>
                {searchHearings.map(h => (
                  <li key={h.id}><strong>{h.hearingDate}</strong> — {h.judgeSummary || '-'}{h.status ? ` — ${h.status}` : ''}</li>
                ))}
              </ul>
            </div>
          )}
        </section>
      )
    }
  ]

  return (
    <SidebarLayout sections={sections} defaultKey="upload" persistKey="sidebar:lawyer" />
  )
}
