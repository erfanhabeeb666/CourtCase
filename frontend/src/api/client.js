const BASE_URL = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

export function authHeaders(token) {
  const headers = {}
  if (token) headers['Authorization'] = `Bearer ${token}`
  return headers
}

// Lawyer APIs
export async function lawyerMyCases(token) {
  const res = await fetch(`${BASE_URL}/lawyer/my-cases`, {
    headers: { ...authHeaders(token) }
  })
  if (!res.ok) throw new Error(await res.text())
  return res.json() // CaseDto[]
}

export async function lawyerGetCase(token, caseId) {
  const res = await fetch(`${BASE_URL}/lawyer/cases/${caseId}`, {
    headers: { ...authHeaders(token) }
  })
  if (!res.ok) throw new Error(await res.text())
  return res.json() // CaseDto
}

export async function lawyerGetHearings(token, caseId) {
  const res = await fetch(`${BASE_URL}/lawyer/cases/${caseId}/hearings`, {
    headers: { ...authHeaders(token) }
  })
  if (!res.ok) throw new Error(await res.text())
  return res.json() // HearingDto[]
}

export async function authenticate(email, password) {
  const res = await fetch(`${BASE_URL}/auth/authenticate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  })
  if (!res.ok) throw new Error(await res.text())
  return res.json() // { token }
}

export async function me(token) {
  const res = await fetch(`${BASE_URL}/auth/me`, {
    headers: { ...authHeaders(token) }
  })
  if (!res.ok) throw new Error(await res.text())
  return res.json() // { id, name, email, userType }
}

// Public registration for Client (no auth header)
export async function registerClientPublic(userDto) {
  const res = await fetch(`${BASE_URL}/auth/register-client`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(userDto)
  })
  if (!res.ok) throw new Error(await res.text())
  return res.text()
}

export async function getHearings(token, caseId) {
  const res = await fetch(`${BASE_URL}/judge/cases/${caseId}/hearings`, {
    headers: {
      ...authHeaders(token)
    }
  })
  if (!res.ok) throw new Error(await res.text())
  return res.json()
}

export async function updateHearing(token, caseId, hearingId, payload) {
  const res = await fetch(`${BASE_URL}/judge/cases/${caseId}/hearings/${hearingId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders(token)
    },
    body: JSON.stringify(payload)
  })
  if (!res.ok) throw new Error(await res.text())
  return res.json()
}

export async function getDocuments(token, caseId) {
  const res = await fetch(`${BASE_URL}/judge/cases/${caseId}/documents`, {
    headers: {
      ...authHeaders(token)
    }
  })
  if (!res.ok) throw new Error(await res.text())
  return res.json()
}

export async function getJudgeTodaysHearings(token) {
  const res = await fetch(`${BASE_URL}/judge/hearings/today`, {
    headers: { ...authHeaders(token) }
  })
  if (!res.ok) throw new Error(await res.text())
  return res.json() // HearingWithCaseDto[]
}

export async function getJudgeMyCases(token) {
  const res = await fetch(`${BASE_URL}/judge/my-cases`, {
    headers: { ...authHeaders(token) }
  })
  if (!res.ok) throw new Error(await res.text())
  return res.json() // CaseDto[]
}

export async function downloadDocument(token, documentId) {
  const res = await fetch(`${BASE_URL}/documents/${documentId}/download`, {
    headers: { ...authHeaders(token) }
  })
  if (!res.ok) throw new Error(await res.text())
  // Try to extract filename from Content-Disposition
  const dispo = res.headers.get('Content-Disposition') || ''
  const match = /filename\*=UTF-8''([^;]+)|filename="?([^";]+)"?/i.exec(dispo)
  const filename = decodeURIComponent(match?.[1] || match?.[2] || 'download')
  const blob = await res.blob()
  return { blob, filename }
}

// Fetch as Blob for inline viewing (e.g., PDF in iframe)
export async function fetchDocumentBlob(token, documentId) {
  const res = await fetch(`${BASE_URL}/documents/${documentId}/download`, {
    headers: { ...authHeaders(token) }
  })
  if (!res.ok) throw new Error(await res.text())
  return await res.blob()
}

export async function uploadDocument(token, { caseId, file, note }) {
  const form = new FormData()
  form.append('caseId', caseId)
  form.append('file', file)
  if (note) form.append('note', note)

  const res = await fetch(`${BASE_URL}/judge/cases/upload-document`, {
    method: 'POST',
    headers: {
      ...authHeaders(token)
    },
    body: form
  })
  if (!res.ok) throw new Error(await res.text())
  return true
}

export async function uploadDocumentForRole(token, role, { caseId, file, note }) {
  const form = new FormData()
  form.append('caseId', caseId)
  form.append('file', file)
  if (note) form.append('note', note)
  const base = role === 'LAWYER' ? 'lawyer' : role === 'JUDGE' ? 'judge' : 'judge'
  const res = await fetch(`${BASE_URL}/${base}/cases/upload-document`, {
    method: 'POST',
    headers: { ...authHeaders(token) },
    body: form
  })
  if (!res.ok) throw new Error(await res.text())
  return true
}

// Admin APIs
export async function adminAddJudge(token, userDto) {
  const res = await fetch(`${BASE_URL}/admin/add-judge`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders(token) },
    body: JSON.stringify(userDto)
  })
  if (!res.ok) throw new Error(await res.text())
  return res.text()
}

export async function adminAssignCase(token, caseId, payload) {
  const res = await fetch(`${BASE_URL}/admin/cases/${caseId}/assign`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...authHeaders(token) },
    body: JSON.stringify(payload)
  })
  if (!res.ok) throw new Error(await res.text())
  return res.json() // CaseDto
}

export async function adminListCases(token) {
  const res = await fetch(`${BASE_URL}/admin/cases`, {
    headers: { ...authHeaders(token) }
  })
  if (!res.ok) throw new Error(await res.text())
  return res.json() // CaseDto[]
}

export async function adminListJudges(token) {
  const res = await fetch(`${BASE_URL}/admin/judges`, {
    headers: { ...authHeaders(token) }
  })
  if (!res.ok) throw new Error(await res.text())
  return res.json() // UserSummaryDto[] (JUDGEs)
}

export async function adminListLawyers(token) {
  const res = await fetch(`${BASE_URL}/admin/lawyers`, {
    headers: { ...authHeaders(token) }
  })
  if (!res.ok) throw new Error(await res.text())
  return res.json() // UserSummaryDto[] (LAWYERs)
}

export async function adminRegisterClient(token, userDto) {
  const res = await fetch(`${BASE_URL}/auth/register-client`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders(token) },
    body: JSON.stringify(userDto)
  })
  if (!res.ok) throw new Error(await res.text())
  return res.text()
}

export async function adminRegisterLawyer(token, userDto) {
  const res = await fetch(`${BASE_URL}/auth/register-lawyer`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders(token) },
    body: JSON.stringify(userDto)
  })
  if (!res.ok) throw new Error(await res.text())
  return res.text()
}

export async function adminListUsers(token, role) {
  const url = role ? `${BASE_URL}/admin/users?role=${encodeURIComponent(role)}` : `${BASE_URL}/admin/users`
  const res = await fetch(url, { headers: { ...authHeaders(token) } })
  if (!res.ok) throw new Error(await res.text())
  return res.json() // UserSummaryDto[]
}

export async function adminUpdateUserStatus(token, userId, status) {
  const res = await fetch(`${BASE_URL}/admin/users/${userId}/status?status=${encodeURIComponent(status)}`, {
    method: 'PATCH',
    headers: { ...authHeaders(token) }
  })
  if (!res.ok) throw new Error(await res.text())
  return res.json() // UserSummaryDto
}

// Client APIs
export async function clientFileCase(token, payload) {
  const res = await fetch(`${BASE_URL}/client/file-case`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders(token) },
    body: JSON.stringify(payload)
  })
  if (!res.ok) throw new Error(await res.text())
  return res.text()
}

export async function clientMyCases(token) {
  const res = await fetch(`${BASE_URL}/client/my-cases`, {
    headers: { ...authHeaders(token) }
  })
  if (!res.ok) throw new Error(await res.text())
  return res.json()
}

export async function clientListLawyers(token) {
  const res = await fetch(`${BASE_URL}/client/lawyers`, {
    headers: { ...authHeaders(token) }
  })
  if (!res.ok) throw new Error(await res.text())
  return res.json() // UserSummaryDto[]
}
