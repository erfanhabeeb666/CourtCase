import React, { useEffect, useState } from 'react'
import { getJudgeMyCases, getHearings, getDocuments, fetchDocumentBlob } from '../api/client'

export default function JudgeMyCases({ token }) {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [cases, setCases] = useState([])
  const [openCaseId, setOpenCaseId] = useState(null)
  const [detailsMap, setDetailsMap] = useState({}) // { [caseId]: { loading, error, hearings, documents } }
  const [viewer, setViewer] = useState({ open: false, url: '', name: '' })

  const loadCases = async () => {
    setLoading(true)
    setError('')
    try {
      const data = await getJudgeMyCases(token)
      setCases(data)
    } catch (e) {
      setError(String(e.message || e))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (token) loadCases()
  }, [token])

  const toggleDetails = async (caseId) => {
    if (openCaseId === caseId) {
      setOpenCaseId(null)
      return
    }
    setOpenCaseId(caseId)
    if (!detailsMap[caseId]) {
      setDetailsMap((prev) => ({ ...prev, [caseId]: { loading: true, error: '', hearings: [], documents: [] } }))
      try {
        const [hearings, documents] = await Promise.all([
          getHearings(token, caseId),
          getDocuments(token, caseId),
        ])
        setDetailsMap((prev) => ({ ...prev, [caseId]: { loading: false, error: '', hearings, documents } }))
      } catch (e) {
        setDetailsMap((prev) => ({ ...prev, [caseId]: { loading: false, error: String(e.message || e), hearings: [], documents: [] } }))
      }
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

  return (
    <section className="card">
      <h2>My Cases</h2>
      <button onClick={loadCases} disabled={!token || loading}>
        {loading ? 'Loading...' : 'Reload'}
      </button>
      {error && <div className="error" style={{ marginTop: 8 }}>{error}</div>}
      <div style={{ margin: '6px 0' }}>
        <small>Loaded {cases.length} cases.</small>
      </div>

      {cases.length > 0 ? (
        <ul>
          {cases.map((c) => (
            <li key={c.id} style={{ marginBottom: 12 }}>
              <div>
                <strong>Case:</strong> {c.title} — <strong>ID:</strong> {c.id} — <strong>Status:</strong> {c.status}
              </div>
              <div>
                <strong>Next Hearing:</strong> {c.nextHearingDate || '-'} {c.verdict ? `— Verdict: ${c.verdict}` : ''}
              </div>
              <div style={{ marginTop: 6 }}>
                <button onClick={() => toggleDetails(c.id)}>
                  {openCaseId === c.id ? 'Hide Details' : 'View Hearings & Documents'}
                </button>
              </div>

              {openCaseId === c.id && (
                <div className="card" style={{ marginTop: 8 }}>
                  {detailsMap[c.id]?.loading && <div>Loading case details...</div>}
                  {detailsMap[c.id]?.error && <div className="error">{detailsMap[c.id]?.error}</div>}
                  {!!detailsMap[c.id] && !detailsMap[c.id].loading && (
                    <>
                      <div className="grid-2">
                        <div>
                          <h4>Hearings</h4>
                          <ul>
                            {detailsMap[c.id].hearings?.map((h) => (
                              <li key={h.id}>
                                <strong>{h.hearingDate}</strong> — {h.judgeSummary || '-'}
                                {h.status ? ` — ${h.status}` : ''}
                              </li>
                            ))}
                          </ul>
                        </div>
                        <div>
                          <h4>Documents</h4>
                          <ul>
                            {detailsMap[c.id].documents?.map((d) => (
                              <li key={d.id}>
                                {d.fileName} — {d.note || ''} — {d.uploadedAt}
                                <button style={{ marginLeft: 8 }} onClick={() => openPdfViewer(d)}>View</button>
                              </li>
                            ))}
                          </ul>
                        </div>
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
}
