# Court Case Management - Frontend (React + Vite)

This is a minimal Judge Panel to interact with the backend APIs.

## Prerequisites

- Node.js 18+
- Backend running on `http://localhost:8080` (or set `VITE_API_BASE`)
- A valid JWT for a user with `JUDGE` authority

## Setup

```bash
# from frontend/
npm install

# optional: set API base
# create .env in frontend/ with:
# VITE_API_BASE=http://localhost:8080

# start dev server
npm run dev
```

Open the printed URL (default: http://localhost:5173) and paste your JWT in the Authentication section.

## Features

- View hearings for a case: `GET /judge/cases/{caseId}/hearings`
- Update hearing / schedule next / submit verdict: `PUT /judge/cases/{caseId}/hearings/{hearingId}`
- View documents for a case: `GET /judge/cases/{caseId}/documents`
- Upload document (multipart): `POST /judge/cases/upload-document`

## Components

- `src/components/HearingsList.jsx`: lists hearings for a case.
- `src/components/UpdateHearingForm.jsx`: unified update form for hearings, next hearing date and verdict.
- `src/components/DocumentsList.jsx`: lists documents for a case.
- `src/components/UploadDocument.jsx`: uploads a file with note to a case.

## Notes

- Ensure CORS is allowed by the backend for the dev origin (http://localhost:5173).
- The frontend sends `Authorization: Bearer <token>` header; paste your JWT in the UI.
- Configure `VITE_API_BASE` if your backend is not on localhost:8080.
