import React, { useEffect, useState } from 'react'
import {
  getJudgeTodaysHearings,
  updateHearing,
  getHearings,
  getDocuments,
  uploadDocument,
  fetchDocumentBlob,
} from '../api/client'

export default function TodaysHearings({ token }) {
  const [items, setItems] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [expandedId, setExpandedId] = useState(null)
  const [busyId, setBusyId] = useState(null)
  const [formStates, setFormStates] = useState({}) // { [hearingId]: { hearingDate, judgeSummary, nextHearingDate, verdict, verdictDate } }
  const [detailsOpenId, setDetailsOpenId] = useState(null)
  const [detailsMap, setDetailsMap] = useState({}) // { [caseId]: { loading, error, hearings, documents } }
  const [uploadMap, setUploadMap] = useState({}) // { [caseId]: { file, note, uploading, error, ok } }
  const [viewer, setViewer] = useState({ open: false, url: '', name: '' })

  useEffect(() => {
    // Auto-load today's hearings when token is available
    if (token) {
      load()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token])

  const load = async () => {
    setError('')
    setLoading(true)
    try {
      const data = await getJudgeTodaysHearings(token)
      setItems(data)
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  const getForm = (hearingId) => {
    return formStates[hearingId] || {
      hearingDate: '',
      judgeSummary: '',
      nextHearingDate: '',
      verdict: '',
      verdictDate: '',
    }
  }

  const setFormField = (hearingId, field, value) => {
    setFormStates((prev) => ({
      ...prev,
      [hearingId]: { ...getForm(hearingId), [field]: value },
    }))
  }

  const submitInlineUpdate = async (hearingId, caseId, form) => {
    setError('')
    setBusyId(hearingId)
    try {
      const payload = {}
      if (form.hearingDate) payload.hearingDate = form.hearingDate
      if (form.judgeSummary) payload.judgeSummary = form.judgeSummary
      if (form.nextHearingDate) payload.nextHearingDate = form.nextHearingDate
      if (form.verdict) payload.verdict = form.verdict
      if (form.verdictDate) payload.verdictDate = form.verdictDate

      await updateHearing(token, caseId, hearingId, payload)
      await load()
      setExpandedId(null)
    } catch (e) {
      setError(e.message)
    } finally {
      setBusyId(null)
    }
  }

  const toggleDetails = async (caseId, open) => {
    if (open) {
      setDetailsOpenId(caseId)
      const existing = detailsMap[caseId]
      if (!existing || (!existing.hearings && !existing.documents)) {
        setDetailsMap((prev) => ({
          ...prev,
          [caseId]: { loading: true, error: '', hearings: [], documents: [] },
        }))
        try {
          const [hearings, documents] = await Promise.all([
            getHearings(token, caseId),
            getDocuments(token, caseId),
          ])
          setDetailsMap((prev) => ({
            ...prev,
            [caseId]: { loading: false, error: '', hearings, documents },
          }))
        } catch (e) {
          setDetailsMap((prev) => ({
            ...prev,
            [caseId]: {
              loading: false,
              error: String(e.message || e),
              hearings: [],
              documents: [],
            },
          }))
        }
      }
    } else {
      setDetailsOpenId(null)
    }
  }

  const setUploadField = (caseId, field, value) => {
    setUploadMap((prev) => ({
      ...prev,
      [caseId]: {
        ...(prev[caseId] || {
          file: null,
          note: '',
          uploading: false,
          error: '',
          ok: false,
        }),
        [field]: value,
      },
    }))
  }

  const submitUpload = async (caseId) => {
    const state = uploadMap[caseId] || { file: null, note: '' }
    if (!state.file) {
      setUploadField(caseId, 'error', 'Please choose a file')
      return
    }
    setUploadField(caseId, 'error', '')
    setUploadField(caseId, 'ok', false)
    setUploadField(caseId, 'uploading', true)
    try {
      await uploadDocument(token, { caseId, file: state.file, note: state.note })
      setUploadField(caseId, 'ok', true)
      // refresh documents if panel is open
      if (detailsOpenId === caseId) {
        try {
          const documents = await getDocuments(token, caseId)
          setDetailsMap((prev) => ({
            ...prev,
            [caseId]: { ...(prev[caseId] || {}), documents },
          }))
        } catch {}
      }
      setUploadField(caseId, 'file', null)
      setUploadField(caseId, 'note', '')
    } catch (e) {
      setUploadField(caseId, 'error', String(e.message || e))
    } finally {
      setUploadField(caseId, 'uploading', false)
    }
  }

  const openPdfViewer = async (doc) => {
    try {
      const blob = await fetchDocumentBlob(token, doc.id)
      const url = URL.createObjectURL(blob)
      setViewer({ open: true, url, name: doc.fileName || `document-${doc.id}.pdf` })
    } catch (e) {
      setError(e.message)
    }
  }

  const closePdfViewer = () => {
    if (viewer.url) URL.revokeObjectURL(viewer.url)
    setViewer({ open: false, url: '', name: '' })
  }

  return (
    <section className="card">
      <h2>Today&apos;s Hearings</h2>
      <button onClick={load} disabled={!token || loading}>
        {loading ? 'Loading...' : 'Load'}
      </button>
      {error && <div className="error">{error}</div>}
      <div style={{ margin: '6px 0' }}>
        <small>Loaded {items.length} hearings for today.</small>
      </div>

      {items.length > 0 ? (
        <ul>
          {items.map((it) => {
            const isOpen = expandedId === it.hearing.id
            const form = getForm(it.hearing.id)
            return (
              <li key={it.hearing.id} style={{ marginBottom: 12 }}>
                <div>
                  <strong>Case:</strong> {it.courtCase.title} — Status: {it.courtCase.status}
                </div>
                <div>
                  <strong>Hearing ID:</strong> {it.hearing.id} — <strong>Date:</strong>{' '}
                  {it.hearing.hearingDate} — <strong>Summary:</strong>{' '}
                  {it.hearing.judgeSummary || '-'}
                </div>
                <div style={{ marginTop: 6 }}>
                  <button onClick={() => setExpandedId(isOpen ? null : it.hearing.id)}>
                    {isOpen ? 'Close' : 'Update this hearing'}
                  </button>
                </div>

                {isOpen && (
                  <div className="card" style={{ marginTop: 8 }}>
                    <div className="form-grid">
                      <label>
                        Hearing Date (YYYY-MM-DD)
                        <input
                          value={form.hearingDate}
                          onChange={(e) =>
                            setFormField(it.hearing.id, 'hearingDate', e.target.value)
                          }
                          placeholder="optional"
                        />
                      </label>
                      <label>
                        Judge Summary
                        <input
                          value={form.judgeSummary}
                          onChange={(e) =>
                            setFormField(it.hearing.id, 'judgeSummary', e.target.value)
                          }
                          placeholder="optional"
                        />
                      </label>
                      <label>
                        Next Hearing Date (YYYY-MM-DD)
                        <input
                          value={form.nextHearingDate}
                          onChange={(e) =>
                            setFormField(it.hearing.id, 'nextHearingDate', e.target.value)
                          }
                          placeholder="optional"
                        />
                      </label>
                      <label>
                        Verdict
                        <input
                          value={form.verdict}
                          onChange={(e) =>
                            setFormField(it.hearing.id, 'verdict', e.target.value)
                          }
                          placeholder="optional"
                        />
                      </label>
                      <label>
                        Verdict Date (YYYY-MM-DD)
                        <input
                          value={form.verdictDate}
                          onChange={(e) =>
                            setFormField(it.hearing.id, 'verdictDate', e.target.value)
                          }
                          placeholder="optional"
                        />
                      </label>
                      <button
                        onClick={() => submitInlineUpdate(it.hearing.id, it.courtCase.id, form)}
                        disabled={busyId === it.hearing.id}
                      >
                        {busyId === it.hearing.id ? 'Updating...' : 'Submit Update'}
                      </button>
                    </div>

                    <div style={{ borderTop: '1px solid var(--muted)', marginTop: 12, paddingTop: 12 }}>
                      <h3 style={{ marginTop: 0 }}>Upload Document to this Case</h3>
                      <div className="form-grid">
                        <label>
                          File
                          <input
                            type="file"
                            accept="application/pdf"
                            onChange={(e) =>
                              setUploadField(
                                it.courtCase.id,
                                'file',
                                e.target.files?.[0] || null
                              )
                            }
                          />
                        </label>
                        <label>
                          Note
                          <input
                            value={uploadMap[it.courtCase.id]?.note || ''}
                            onChange={(e) => setUploadField(it.courtCase.id, 'note', e.target.value)}
                            placeholder="optional"
                          />
                        </label>
                        <button
                          onClick={() => submitUpload(it.courtCase.id)}
                          disabled={uploadMap[it.courtCase.id]?.uploading}
                        >
                          {uploadMap[it.courtCase.id]?.uploading ? 'Uploading...' : 'Upload'}
                        </button>
                      </div>
                      {uploadMap[it.courtCase.id]?.ok && <div className="success">Uploaded</div>}
                      {uploadMap[it.courtCase.id]?.error && (
                        <div className="error">{uploadMap[it.courtCase.id]?.error}</div>
                      )}
                    </div>

                    <div style={{ borderTop: '1px solid var(--muted)', marginTop: 12, paddingTop: 12 }}>
                      <button
                        onClick={() =>
                          toggleDetails(it.courtCase.id, detailsOpenId !== it.courtCase.id)
                        }
                      >
                        {detailsOpenId === it.courtCase.id ? 'Hide Case Details' : 'View Case Details'}
                      </button>

                      {detailsOpenId === it.courtCase.id && (
                        <div style={{ marginTop: 10 }}>
                          {detailsMap[it.courtCase.id]?.loading && <div>Loading case details...</div>}
                          {detailsMap[it.courtCase.id]?.error && (
                            <div className="error">{detailsMap[it.courtCase.id]?.error}</div>
                          )}
                          {!!detailsMap[it.courtCase.id] && !detailsMap[it.courtCase.id].loading && (
                            <>
                              <div className="grid-2">
                                <div>
                                  <h4>All Hearings</h4>
                                  <ul>
                                    {detailsMap[it.courtCase.id].hearings?.map((h) => (
                                      <li key={h.id}>
                                        <strong>{h.hearingDate}</strong> — {h.judgeSummary || '-'}
                                      </li>
                                    ))}
                                  </ul>
                                </div>
                                <div>
                                  <h4>Documents</h4>
                                  <ul>
                                    {detailsMap[it.courtCase.id].documents?.map((d) => (
                                      <li key={d.id}>
                                        {d.fileName} — {d.note || ''} — {d.uploadedAt}
                                        <button
                                          style={{ marginLeft: 8 }}
                                          onClick={() => openPdfViewer(d)}
                                        >
                                          View
                                        </button>
                                      </li>
                                    ))}
                                  </ul>
                                </div>
                              </div>

                              {viewer.open && (
                                <div className="card" style={{ marginTop: 12 }}>
                                  <div
                                    style={{
                                      display: 'flex',
                                      justifyContent: 'space-between',
                                      alignItems: 'center',
                                    }}
                                  >
                                    <h3 style={{ margin: 0 }}>{viewer.name}</h3>
                                    <button onClick={closePdfViewer}>Close</button>
                                  </div>
                                  <div
                                    style={{
                                      height: 600,
                                      marginTop: 8,
                                      border: '1px solid var(--muted)',
                                    }}
                                  >
                                    <iframe
                                      title="PDF Viewer"
                                      src={viewer.url}
                                      style={{ width: '100%', height: '100%', border: 'none' }}
                                    />
                                  </div>
                                </div>
                              )}
                            </>
                          )}
                        </div>
                      )}
                    </div>
                  </div>
                )}
              </li>
            )
          })}
        </ul>
      ) : (
        <p>No hearings loaded.</p>
      )}
    </section>
  )
}