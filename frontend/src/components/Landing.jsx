import React from 'react'

export default function Landing({ onGetStarted, onSignIn }) {
  return (
    <div className="landing">
      <section className="hero card">
        <h1>Court Case Management System</h1>
        <p>
          Streamline case filing, scheduling, hearings, and document workflows for Admins, Judges, Lawyers and Clients.
        </p>
        <div className="cta-row">
          <button onClick={onGetStarted}>Get Started</button>
          <button onClick={onSignIn} className="secondary">Sign In</button>
        </div>
      </section>

      <section className="card section">
        <h2>Why choose CCM?</h2>
        <div className="features-grid">
          <Feature icon="⚖️" title="Role-based Dashboards" desc="Tailored views for Admin, Judge, Lawyer, and Client with focused workflows." />
          <Feature icon="🗓️" title="Hearing Scheduling" desc="Manage hearings, see today's list, and keep everyone on the same page." />
          <Feature icon="📄" title="Secure Documents" desc="Upload and download case documents securely, with role-aware access." />
          <Feature icon="🧭" title="Case Assignment" desc="Admins can assign cases to judges and lawyers in a couple of clicks." />
          <Feature icon="🔐" title="Modern Auth" desc="Email-based authentication with JWT and granular authorization." />
          <Feature icon="🚀" title="Fast & Simple" desc="Minimal clicks, clean design, and a distraction-free experience." />
        </div>
      </section>

      <section className="card section">
        <h2>Who is it for?</h2>
        <div className="roles-grid">
          <RoleCard icon="👑" title="Admin" items={[
            'Add Judges and manage users',
            'Assign cases and control statuses',
            'Monitor system-wide activity',
          ]} />
          <RoleCard icon="⚖️" title="Judge" items={[
            "See today's hearings",
            'Review documents & case details',
            'Manage outcomes efficiently',
          ]} />
          <RoleCard icon="🧑‍💼" title="Lawyer" items={[
            'Access assigned cases',
            'Upload and view documents',
            'Coordinate with opposing counsel',
          ]} />
          <RoleCard icon="🧑‍💻" title="Client" items={[
            'File a new case',
            'Track progress and hearings',
            'Upload supporting documents',
          ]} />
        </div>
      </section>

      <section className="card section centered">
        <h2>Ready to simplify court workflows?</h2>
        <p>Create an account or sign in to continue.</p>
        <div className="cta-row">
          <button onClick={onGetStarted}>Create Account</button>
          <button onClick={onSignIn} className="secondary">I already have an account</button>
        </div>
      </section>
    </div>
  )
}

function Feature({ icon, title, desc }) {
  return (
    <div className="feature">
      <div className="icon">{icon}</div>
      <div className="title">{title}</div>
      <div className="desc">{desc}</div>
    </div>
  )
}

function RoleCard({ icon, title, items }) {
  return (
    <div className="role-card">
      <div className="head">
        <div className="icon">{icon}</div>
        <div className="title">{title}</div>
      </div>
      <ul>
        {items.map((i, idx) => (
          <li key={idx}>{i}</li>
        ))}
      </ul>
    </div>
  )
}
