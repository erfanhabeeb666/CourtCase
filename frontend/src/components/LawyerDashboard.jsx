import React, { useEffect, useState } from 'react'
import { uploadDocumentForRole, lawyerMyCases, lawyerGetCase, lawyerGetHearings, lawyerGetDocuments, fetchDocumentBlob, downloadDocument, me, deleteDocument } from '../api/client'
import SidebarLayout from './SidebarLayout'

export default function LawyerDashboard({ token }) {
  const [currentUser, setCurrentUser] = useState(null)
  const [caseId, setCaseId] = useState('')
  const [file, setFile] = useState(null)
  const [note, setNote] = useState('')
  const [ok, setOk] = useState(false)
  const [error, setError] = useState('')
  // Upload tab documents preview just below the form
  const [uploadDocs, setUploadDocs] = useState([])
  const [uploadDocsErr, setUploadDocsErr] = useState('')
  const [uploadDocsLoading, setUploadDocsLoading] = useState(false)

  // Load existing documents when a case ID is entered/changed in the upload tab
  useEffect(() => {
    const load = async () => {
      if (!token || !caseId) { setUploadDocs([]); setUploadDocsErr(''); return }
      setUploadDocsErr(''); setUploadDocsLoading(true)
      try {
        const docs = await lawyerGetDocuments(token, caseId)
        setUploadDocs(docs)
      } catch (e) {
        setUploadDocsErr(String(e.message || e))
      } finally {
        setUploadDocsLoading(false)
      }
    }
    load()
  }, [token, caseId])

  // Load current user to filter "my uploads"
  useEffect(() => {
    let active = true
    const loadMe = async () => {
      if (!token) { setCurrentUser(null); return }
      try { const u = await me(token); if (active) setCurrentUser(u) } catch { /* ignore */ }
    }
    loadMe()
    return () => { active = false }
  }, [token])

  const removeDocFromCase = (cid, docId) => {
    setDetailsMap(prev => ({
      ...prev,
      [cid]: {
        ...(prev[cid] || {}),
        documents: (prev[cid]?.documents || []).filter(d => d.id !== docId)
      }
    }))
  }

  const onDeleteDoc = async (doc, cid) => {
    try {
      if (!window.confirm('Delete this document?')) return
      await deleteDocument(token, doc.id)
      removeDocFromCase(cid, doc.id)
      // Also remove from upload tab list if visible
      setUploadDocs(prev => (prev || []).filter(d => d.id !== doc.id))
    } catch (e) {
      setError(String(e.message || e))
    }
  }
  // My Cases state
  const [myCases, setMyCases] = useState([])
  const [myCasesLoading, setMyCasesLoading] = useState(false)
  const [myCasesErr, setMyCasesErr] = useState('')
  const [page, setPage] = useState(1)
  const pageSize = 5
  const [myCasesSearch, setMyCasesSearch] = useState('')
  const [openCaseId, setOpenCaseId] = useState(null)
  const [detailsMap, setDetailsMap] = useState({}) // { [caseId]: { loading, error, hearings, documents } }
  
  // Search Case state
  const [searchId, setSearchId] = useState('')
  const [searchCase, setSearchCase] = useState(null)
  const [searchHearings, setSearchHearings] = useState([])
  const [searchDocuments, setSearchDocuments] = useState([])
  const [searchErr, setSearchErr] = useState('')
  const [viewer, setViewer] = useState({ open: false, url: '', name: '', docId: null, caseId: null })

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
      // refresh documents if this case panel is open (like judge view)
      if (openCaseId === cid) {
        try {
          const documents = await lawyerGetDocuments(token, cid)
          setDetailsMap(prev => ({ ...prev, [cid]: { ...(prev[cid] || {}), documents } }))
        } catch {}
      }
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
      // Keep caseId as requested; only clear selected file and note
      setFile(null); setNote('')
      // After successful upload, load documents for this case and show below
      if (caseId) {
        setUploadDocsErr(''); setUploadDocsLoading(true)
        try {
          const docs = await lawyerGetDocuments(token, caseId)
          setUploadDocs(docs)
        } catch (e) {
          setUploadDocsErr(String(e.message || e))
        } finally {
          setUploadDocsLoading(false)
        }
      }
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
      setPage(1)
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
      setDetailsMap(prev => ({ ...prev, [cid]: { loading: true, error: '', hearings: [], documents: [] } }))
      try {
        const [hearings, documents] = await Promise.all([
          lawyerGetHearings(token, cid),
          lawyerGetDocuments(token, cid)
        ])
        setDetailsMap(prev => ({ ...prev, [cid]: { loading: false, error: '', hearings, documents } }))
      } catch (e) {
        setDetailsMap(prev => ({ ...prev, [cid]: { loading: false, error: String(e.message || e), hearings: [], documents: [] } }))
      }
    }
  }

  const doSearch = async (e) => {
    e?.preventDefault?.()
    setSearchErr(''); setSearchCase(null); setSearchHearings([]); setSearchDocuments([])
    if (!searchId) { setSearchErr('Enter case ID'); return }
    try {
      const c = await lawyerGetCase(token, searchId)
      setSearchCase(c)
      const [hs, ds] = await Promise.all([
        lawyerGetHearings(token, searchId),
        lawyerGetDocuments(token, searchId)
      ])
      setSearchHearings(hs)
      setSearchDocuments(ds)
    } catch (e) {
      setSearchErr(String(e.message || e))
    }
  }

  const openPdfViewer = async (doc, caseId) => {
    try {
      if (viewer.url) URL.revokeObjectURL(viewer.url)
      const blob = await fetchDocumentBlob(token, doc.id)
      const url = URL.createObjectURL(blob)
      setViewer({ open: true, url, name: doc.fileName || `document-${doc.id}.pdf`, docId: doc.id, caseId })
    } catch (e) {
      setError(String(e.message || e))
    }
  }

  const download = async (doc) => {
    try {
      const { blob, filename } = await downloadDocument(token, doc.id)
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = filename
      document.body.appendChild(a)
      a.click()
      a.remove()
      URL.revokeObjectURL(url)
    } catch (e) {
      setError(String(e.message || e))
    }
  }

  const closePdfViewer = () => {
    if (viewer.url) URL.revokeObjectURL(viewer.url)
    setViewer({ open: false, url: '', name: '', docId: null, caseId: null })
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
              <input type="text" value={caseId} onChange={(e) => setCaseId(e.target.value)} required />
            </label>
            <label>
              File
              <input type="file" accept="application/pdf" onChange={(e) => setFile(e.target.files?.[0])} required />
            </label>
            <label>
              Note
              <input value={note} onChange={(e) => setNote(e.target.value)} placeholder="optional" />
            </label>
            <button type="submit" disabled={!token || !caseId || !file}>Upload</button>
          </form>
          {ok && <div className="success">Uploaded</div>}
          {error && <div className="error">{error}</div>}

          <div style={{ borderTop: '1px solid var(--muted)', marginTop: 12, paddingTop: 12 }}>
            <h4 style={{ marginTop: 0 }}>Documents for Case {caseId || '(enter ID and upload)'}</h4>
            {uploadDocsErr && <div className="error">{uploadDocsErr}</div>}
            {uploadDocsLoading && <div>Loading documents...</div>}
            {!uploadDocsLoading && (
              <ul>
                {uploadDocs.map(d => (
                  <li key={d.id}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      <span>{d.fileName} — {d.note || ''} — {d.uploadedAt}</span>
                      <span style={{ marginLeft: 'auto', display: 'inline-flex', gap: 6 }}>
                        <button className="btn-inline" onClick={() => openPdfViewer(d, caseId || null)}>View</button>
                        <button className="btn-inline" onClick={() => download(d)}>Download</button>
                        {currentUser && d.uploaderId === currentUser.id && (
                          <button className="btn-inline danger" onClick={() => onDeleteDoc(d, caseId || null)}>Delete</button>
                        )}
                      </span>
                    </div>
                    {viewer.open && viewer.docId === d.id && (viewer.caseId === caseId || viewer.caseId == null) && (
                      <div className="card" style={{ marginTop: 8 }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <h3 style={{ margin: 0 }}>{viewer.name}</h3>
                          <button onClick={closePdfViewer}>Close</button>
                        </div>
                        <div style={{ height: 600, marginTop: 8, border: '1px solid var(--muted)' }}>
                          <iframe title="PDF Viewer" src={viewer.url} style={{ width: '100%', height: '100%', border: 'none' }} />
                        </div>
                      </div>
                    )}
                  </li>
                ))}
                {uploadDocs.length === 0 && (
                  <li>No uploaded files</li>
                )}
              </ul>
            )}
            {viewer.open && (
              <div className="card" style={{ marginTop: 12 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <h3 style={{ margin: 0 }}>{viewer.name}</h3>
                  <button onClick={closePdfViewer}>Close</button>
                </div>
                <div style={{ height: 600, marginTop: 8, border: '1px solid var(--muted)' }}>
                  <iframe title="PDF Viewer" src={viewer.url} style={{ width: '100%', height: '100%', border: 'none' }} />
                </div>
              </div>
            )}
          </div>
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
          <div className="form-grid" style={{ alignItems: 'end' }}>
            <label>
              Search (ID or Title)
              <input value={myCasesSearch} onChange={(e) => { setMyCasesSearch(e.target.value); setPage(1) }} placeholder="Type to filter" />
            </label>
            <div>
              <button onClick={loadMyCases} disabled={!token || myCasesLoading}>{myCasesLoading ? 'Loading...' : 'Reload'}</button>
            </div>
          </div>
          {myCasesErr && <div className="error" style={{ marginTop: 8 }}>{myCasesErr}</div>}
          <div style={{ margin: '6px 0' }}>
            <small>Loaded {myCases.length} cases.</small>
          </div>
          {myCases.length > 0 ? (
            <ul>
              {(() => {
                const needle = (myCasesSearch || '').toLowerCase()
                const filtered = myCases.filter(c =>
                  !needle || String(c.id).toLowerCase().includes(needle) || (c.title || '').toLowerCase().includes(needle)
                )
                const total = filtered.length
                const totalPages = Math.max(1, Math.ceil(total / pageSize))
                const curPage = Math.min(page, totalPages)
                const start = (curPage - 1) * pageSize
                const paginated = filtered.slice(start, start + pageSize)
                return paginated
              })().map(c => (
                <li key={c.id} style={{ marginBottom: 12 }}>
                  <div><strong>Case:</strong> {c.title} — <strong>ID:</strong> {c.id} — <strong>Status:</strong> {c.status}</div>
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
                            <section className="card">
                              <h4 style={{ marginTop: 0 }}>My Uploads</h4>
                              <ul>
                              {(detailsMap[c.id].documents || []).filter(d => currentUser && d.uploaderId === currentUser.id).map((d) => (
                                <li key={d.id}>
                                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                    <span>{d.fileName} — {d.note || ''} — {d.uploadedAt}</span>
                                    <span style={{ marginLeft: 'auto', display: 'inline-flex', gap: 6 }}>
                                      <button className="btn-inline" onClick={() => openPdfViewer(d, c.id)}>View</button>
                                      <button className="btn-inline" onClick={() => download(d)}>Download</button>
                                      <button className="btn-inline danger" onClick={() => onDeleteDoc(d, c.id)}>Delete</button>
                                    </span>
                                  </div>
                                  {viewer.open && viewer.docId === d.id && viewer.caseId === c.id && (
                                    <div className="card" style={{ marginTop: 8 }}>
                                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                        <h3 style={{ margin: 0 }}>{viewer.name}</h3>
                                        <button onClick={closePdfViewer}>Close</button>
                                      </div>
                                      <div style={{ height: 600, marginTop: 8, border: '1px solid var(--muted)' }}>
                                        <iframe title="PDF Viewer" src={viewer.url} style={{ width: '100%', height: '100%', border: 'none' }} />
                                      </div>
                                    </div>
                                  )}
                                </li>
                              ))}
                              {(!currentUser || (detailsMap[c.id].documents || []).filter(d => d.uploaderId === currentUser.id).length === 0) && (
                                <li>No uploaded files</li>
                              )}
                              </ul>
                            </section>

                            <section className="card" style={{ marginTop: 12 }}>
                              <h4 style={{ marginTop: 0 }}>All Documents</h4>
                              <ul>
                              {(detailsMap[c.id].documents || []).map((d) => (
                                <li key={d.id}>
                                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                    <span>
                                      {d.fileName} — {d.note || ''} — {d.uploadedAt}
                                      {currentUser && d.uploaderId === currentUser.id && (
                                        <span style={{ marginLeft: 8, color: 'var(--muted-foreground)' }}>(You uploaded)</span>
                                      )}
                                    </span>
                                    <span style={{ marginLeft: 'auto', display: 'inline-flex', gap: 6 }}>
                                      <button className="btn-inline" onClick={() => openPdfViewer(d, c.id)}>View</button>
                                      <button className="btn-inline" onClick={() => download(d)}>Download</button>
                                    </span>
                                  </div>
                                  {viewer.open && viewer.docId === d.id && viewer.caseId === c.id && (
                                    <div className="card" style={{ marginTop: 8 }}>
                                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                        <h3 style={{ margin: 0 }}>{viewer.name}</h3>
                                        <button onClick={closePdfViewer}>Close</button>
                                      </div>
                                      <div style={{ height: 600, marginTop: 8, border: '1px solid var(--muted)' }}>
                                        <iframe title="PDF Viewer" src={viewer.url} style={{ width: '100%', height: '100%', border: 'none' }} />
                                      </div>
                                    </div>
                                  )}
                                </li>
                              ))}
                              {(!detailsMap[c.id].documents || detailsMap[c.id].documents.length === 0) && (
                                <li>No uploaded files</li>
                              )}
                              </ul>
                            </section>
                          </div>
                          {viewer.open && (
                            <div className="card" style={{ marginTop: 12 }}>
                              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <h3 style={{ margin: 0 }}>{viewer.name}</h3>
                                <button onClick={closePdfViewer}>Close</button>
                              </div>
                              <div style={{ height: 600, marginTop: 8, border: '1px solid var(--muted)' }}>
                                <iframe title="PDF Viewer" src={viewer.url} style={{ width: '100%', height: '100%', border: 'none' }} />
                              </div>
                            </div>
                          )}

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

          {(() => {
            const needle = (myCasesSearch || '').toLowerCase()
            const total = myCases.filter(c => !needle || String(c.id).toLowerCase().includes(needle) || (c.title || '').toLowerCase().includes(needle)).length
            const totalPages = Math.max(1, Math.ceil(total / pageSize))
            if (total === 0) return null
            return (
              <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginTop: 8 }}>
                <button onClick={() => setPage(p => Math.max(1, p - 1))} disabled={page <= 1}>Prev</button>
                <span>Page {page} of {totalPages}</span>
                <button onClick={() => setPage(p => Math.min(totalPages, p + 1))} disabled={page >= totalPages}>Next</button>
              </div>
            )
          })()}
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
              <input type="text" value={searchId} onChange={(e) => setSearchId(e.target.value)} />
            </label>
            <button type="submit" disabled={!token || !searchId}>Search</button>
          </form>
          {searchErr && <div className="error">{searchErr}</div>}
          {searchCase && (
            <div className="card" style={{ marginTop: 8 }}>
              <div><strong>Case:</strong> {searchCase.title} — <strong>ID:</strong> {searchCase.id} — <strong>Status:</strong> {searchCase.status}</div>
              <div><strong>Next Hearing:</strong> {searchCase.nextHearingDate || '-'}</div>
              <h4 style={{ marginTop: 8 }}>Hearings</h4>
              <ul>
                {searchHearings.map(h => (
                  <li key={h.id}><strong>{h.hearingDate}</strong> — {h.judgeSummary || '-'}{h.status ? ` — ${h.status}` : ''}</li>
                ))}
              </ul>
              <div style={{ borderTop: '1px solid var(--muted)', marginTop: 12, paddingTop: 12 }}>
                <h4 style={{ marginTop: 0 }}>Documents</h4>
                <ul>
                  {(searchDocuments || []).map(d => (
                    <li key={d.id}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <span>
                          {d.fileName} — {d.note || ''} — {d.uploadedAt}
                          {currentUser && d.uploaderId === currentUser.id && (
                            <span style={{ marginLeft: 8, color: 'var(--muted-foreground)' }}>(You uploaded)</span>
                          )}
                        </span>
                        <span style={{ marginLeft: 'auto', display: 'inline-flex', gap: 6 }}>
                          <button className="btn-inline" onClick={() => openPdfViewer(d, searchCase?.id || searchId)}>View</button>
                          <button className="btn-inline" onClick={() => download(d)}>Download</button>
                        </span>
                      </div>
                      {viewer.open && viewer.docId === d.id && viewer.caseId === (searchCase?.id || searchId) && (
                        <div className="card" style={{ marginTop: 8 }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <h3 style={{ margin: 0 }}>{viewer.name}</h3>
                            <button onClick={closePdfViewer}>Close</button>
                          </div>
                          <div style={{ height: 600, marginTop: 8, border: '1px solid var(--muted)' }}>
                            <iframe title="PDF Viewer" src={viewer.url} style={{ width: '100%', height: '100%', border: 'none' }} />
                          </div>
                        </div>
                      )}
                    </li>
                  ))}
                  {(!searchDocuments || searchDocuments.length === 0) && (
                    <li>No uploaded files</li>
                  )}
                </ul>
              </div>
              {/* Inline viewer now renders beneath the selected document item */}
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
