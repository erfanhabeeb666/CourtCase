import React, { useState } from 'react'
import { getHearings } from '../api/client'

export default function HearingsList({ token, caseId }) {
  const [items, setItems] = useState([])
  const [error, setError] = useState('')

  const load = async () => {
    setError('')
    try {
      const data = await getHearings(token, caseId)
      setItems(data)
    } catch (e) {
      setError(e.message)
    }
  }

  return (
    <section className="card">
      <h2>Hearings</h2>
      <button onClick={load} disabled={!caseId}>Load Hearings</button>
      {error && <div className="error">{error}</div>}
      <ul>
        {items.map(h => (
          <li key={h.id}>
            <strong>ID:</strong> {h.id} &nbsp; 
            <strong>Date:</strong> {h.hearingDate} &nbsp; 
            <strong>Summary:</strong> {h.judgeSummary || '-'}
          </li>
        ))}
      </ul>
    </section>
  )
}
