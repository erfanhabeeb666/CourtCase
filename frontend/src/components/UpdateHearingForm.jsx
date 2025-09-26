import React, { useState } from 'react'
import { updateHearing } from '../api/client'

export default function UpdateHearingForm({ token, caseId, hearingId }) {
  const [hearingDate, setHearingDate] = useState('')
  const [judgeSummary, setJudgeSummary] = useState('')
  const [nextHearingDate, setNextHearingDate] = useState('')
  const [verdict, setVerdict] = useState('')
  const [verdictDate, setVerdictDate] = useState('')
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')
  const todayStr = new Date().toISOString().slice(0, 10)

  const onSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setResult(null)
    try {
      const payload = {}
      if (hearingDate) payload.hearingDate = hearingDate
      if (judgeSummary) payload.judgeSummary = judgeSummary
      if (nextHearingDate) payload.nextHearingDate = nextHearingDate
      if (verdict) payload.verdict = verdict
      if (verdictDate) payload.verdictDate = verdictDate

      const data = await updateHearing(token, caseId, hearingId, payload)
      setResult(data)
    } catch (e) {
      setError(e.message)
    }
  }

  return (
    <section className="card">
      <h2>Update Hearing</h2>
      <form onSubmit={onSubmit} className="form-grid">
        <label>
          Hearing Date
          <input type="date" value={hearingDate} onChange={(e) => setHearingDate(e.target.value)} placeholder="optional" />
        </label>
        <label>
          Judge Summary
          <input value={judgeSummary} onChange={(e) => setJudgeSummary(e.target.value)} placeholder="optional" />
        </label>
        <label>
          Next Hearing Date
          <input type="date" value={nextHearingDate} min={todayStr} onChange={(e) => setNextHearingDate(e.target.value)} placeholder="optional" />
        </label>
        <label>
          Verdict
          <input value={verdict} onChange={(e) => setVerdict(e.target.value)} placeholder="optional" />
        </label>
        <label>
          Verdict Date
          <input type="date" value={verdictDate} onChange={(e) => setVerdictDate(e.target.value)} placeholder="optional" />
        </label>
        <button type="submit" disabled={!token || !caseId || !hearingId}>Submit</button>
      </form>
      {error && <div className="error">{error}</div>}
      {result && (
        <pre className="result">{JSON.stringify(result, null, 2)}</pre>
      )}
    </section>
  )
}
