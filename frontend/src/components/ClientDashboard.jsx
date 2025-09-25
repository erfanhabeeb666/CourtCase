import React, { useEffect, useState } from 'react'
import { clientFileCase, clientMyCases, clientListLawyers, clientUploadDocument, clientGetDocuments, fetchDocumentBlob, downloadDocument } from '../api/client'
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

  // Documents per case (client view)
  const [openCaseId, setOpenCaseId] = useState(null)
  const [docsMap, setDocsMap] = useState({}) // { [caseId]: { loading, error, documents: [] } }
  const [viewer, setViewer] = useState({ open: false, url: '', name: '' })

  // Upload state per case
  const [uploadNotes, setUploadNotes] = useState({}) // { [caseId]: note }
  const [uploadFiles, setUploadFiles] = useState({}) // { [caseId]: FileList or File[] }
  const [uploadOk, setUploadOk] = useState({}) // { [caseId]: boolean }
  const [uploadErr, setUploadErr] = useState({}) // { [caseId]: string }

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

  const onSelectFiles = (caseId, fileList) => {
    setUploadFiles(prev => ({ ...prev, [caseId]: Array.from(fileList || []) }))
    setUploadOk(prev => ({ ...prev, [caseId]: false }))
    setUploadErr(prev => ({ ...prev, [caseId]: '' }))
  }
  const onChangeNote = (caseId, note) => {
    setUploadNotes(prev => ({ ...prev, [caseId]: note }))
  }
  const onUpload = async (caseId) => {
    setUploadErr(prev => ({ ...prev, [caseId]: '' }))
    setUploadOk(prev => ({ ...prev, [caseId]: false }))
    try {
      const files = uploadFiles[caseId]
      const note = uploadNotes[caseId]
      if (!files || files.length === 0) throw new Error('Please select at least one file')
      await clientUploadDocument(token, { caseId, files, note })
      setUploadOk(prev => ({ ...prev, [caseId]: true }))
      // reset files but keep note
      setUploadFiles(prev => ({ ...prev, [caseId]: [] }))
      // Refresh documents if panel is open
      if (openCaseId === caseId) {
        try {
          const documents = await clientGetDocuments(token, caseId)
          setDocsMap(prev => ({ ...prev, [caseId]: { ...(prev[caseId] || {}), loading: false, error: '', documents } }))
        } catch (e) {
          setDocsMap(prev => ({ ...prev, [caseId]: { loading: false, error: String(e.message || e), documents: [] } }))
        }
      }
    } catch (e) {
      setUploadErr(prev => ({ ...prev, [caseId]: e.message }))
    }
  }

  const toggleDocs = async (cid) => {
    console.log('[ClientDashboard] toggleDocs clicked for case', cid)
    if (openCaseId === cid) { setOpenCaseId(null); return }
    setOpenCaseId(cid)
    // Always fetch on open to ensure fresh list
    setDocsMap(prev => ({ ...prev, [cid]: { loading: true, error: '', documents: [] } }))
    try {
      console.log('[ClientDashboard] fetching client documents for case', cid)
      const documents = await clientGetDocuments(token, cid)
      console.log('[ClientDashboard] fetched documents count', documents?.length)
      setDocsMap(prev => ({ ...prev, [cid]: { loading: false, error: '', documents } }))
    } catch (e) {
      console.error('[ClientDashboard] fetch documents error', e)
      setDocsMap(prev => ({ ...prev, [cid]: { loading: false, error: String(e.message || e), documents: [] } }))
    }
  }

  const openPdfViewer = async (doc) => {
    try {
      const blob = await fetchDocumentBlob(token, doc.id)
      const url = URL.createObjectURL(blob)
      setViewer({ open: true, url, name: doc.fileName || `document-${doc.id}.pdf` })
    } catch (e) {
      setListErr(String(e.message || e))
    }
  }

  const closePdfViewer = () => {
    if (viewer.url) URL.revokeObjectURL(viewer.url)
    setViewer({ open: false, url: '', name: '' })
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
      setListErr(String(e.message || e))
    }
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
            <label>
              Type
              <select value={type} onChange={(e) => setType(e.target.value)}>
                <option value="">Select type</option>
                <option value="CIVIL">CIVIL</option>
                <option value="CRIMINAL">CRIMINAL</option>
              </select>
            </label>
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
                <li key={c.id}>
                  <div><strong>{c.title}</strong> — <strong>ID:</strong> {c.id} — Status: {c.status} — Next: {c.nextHearingDate || '-'}</div>
                  <button style={{ marginTop: 6 }} onClick={() => toggleDocs(c.id)}>
                    {openCaseId === c.id ? 'Hide Documents' : 'View Documents'}
                  </button>
                  {openCaseId === c.id && (
                    <div className="card" style={{ marginTop: 8 }}>
                      {docsMap[c.id]?.loading && <div>Loading documents...</div>}
                      {docsMap[c.id]?.error && <div className="error">{docsMap[c.id]?.error}</div>}
                      {!!docsMap[c.id] && !docsMap[c.id].loading && (
                        <>
                          <ul>
                            {(docsMap[c.id].documents || []).map(d => (
                              <li key={d.id}>
                                {d.fileName} — {d.note || ''} — {d.uploadedAt}
                                <button style={{ marginLeft: 8 }} onClick={() => openPdfViewer(d)}>View</button>
                                <button style={{ marginLeft: 8 }} onClick={() => download(d)}>Download</button>
                              </li>
                            ))}
                            {(!docsMap[c.id].documents || docsMap[c.id].documents.length === 0) && (
                              <li>No uploaded files</li>
                            )}
                          </ul>

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
                  {(c.status === 'NEW' || c.status === 'ASSIGNED') && (
                    <>
                      <div className="upload-inline">
                        <label>
                          Select files
                          <input type="file" multiple onChange={(e) => onSelectFiles(c.id, e.target.files)} />
                        </label>
                        <label>
                          Note
                          <input placeholder="optional" value={uploadNotes[c.id] || ''} onChange={(e) => onChangeNote(c.id, e.target.value)} />
                        </label>
                        <button onClick={() => onUpload(c.id)} disabled={!token || !(uploadFiles[c.id]?.length > 0)}>Upload</button>
                      </div>
                      {uploadOk[c.id] && <div className="success">Uploaded</div>}
                      {uploadErr[c.id] && <div className="error">{uploadErr[c.id]}</div>}
                    </>
                  )}
                </li>
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
