import { useState } from 'react'

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export default function App() {
  const [mode, setMode] = useState('login')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const [loginForm, setLoginForm] = useState({ email: '', password: '' })
  const [registerForm, setRegisterForm] = useState({ name: '', email: '', password: '', confirmPassword: '' })

  const handleLogin = async (e) => {
    e.preventDefault()
    setError(''); setSuccess('')
    setLoading(true)
    try {
      const res = await fetch(`${API_BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: loginForm.email, password: loginForm.password })
      })
      const data = await res.json()
      if (!res.ok) throw new Error(data.message || 'Login failed')
      localStorage.setItem('token', data.token)
      setSuccess('Login successful! Redirecting...')
      setTimeout(() => { window.location.href = '/dashboard' }, 1200)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleRegister = async (e) => {
    e.preventDefault()
    setError(''); setSuccess('')
    if (registerForm.password !== registerForm.confirmPassword) {
      return setError('Passwords do not match')
    }
    setLoading(true)
    try {
      const res = await fetch(`${API_BASE}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: registerForm.name, email: registerForm.email, password: registerForm.password })
      })
      const data = await res.json()
      if (!res.ok) throw new Error(data.message || 'Registration failed')
      setSuccess('Account created! Please login.')
      setTimeout(() => { setMode('login'); setSuccess('') }, 1500)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=DM+Sans:wght@300;400;500&display=swap');
        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
        :root {
          --ink: #1a1a2e; --gold: #c9a84c; --cream: #faf8f3;
          --muted: #8a8070; --border: #e2d9c8;
          --error: #c0392b; --success: #27ae60;
        }
        body { background: var(--cream); font-family: 'DM Sans', sans-serif; min-height: 100vh; }
        .page { min-height: 100vh; display: grid; grid-template-columns: 1fr 1fr; }
        .left {
          background: var(--ink); display: flex; flex-direction: column;
          justify-content: center; padding: 60px; position: relative; overflow: hidden;
        }
        .left::before {
          content: ''; position: absolute; top: -100px; right: -100px;
          width: 400px; height: 400px; border-radius: 50%;
          background: radial-gradient(circle, rgba(201,168,76,0.15) 0%, transparent 70%);
        }
        .brand { display: flex; align-items: center; gap: 12px; margin-bottom: 64px; }
        .brand-icon {
          width: 40px; height: 40px; background: var(--gold); border-radius: 10px;
          display: flex; align-items: center; justify-content: center; font-size: 20px;
        }
        .brand-name { font-family: 'Playfair Display', serif; color: #fff; font-size: 20px; }
        .left-tagline { font-family: 'Playfair Display', serif; color: #fff; font-size: 42px; line-height: 1.2; margin-bottom: 24px; }
        .left-tagline span { color: var(--gold); }
        .left-desc { color: rgba(255,255,255,0.55); font-size: 15px; line-height: 1.7; max-width: 360px; }
        .features { margin-top: 48px; display: flex; flex-direction: column; gap: 16px; }
        .feature { display: flex; align-items: center; gap: 14px; }
        .feature-dot { width: 8px; height: 8px; background: var(--gold); border-radius: 50%; flex-shrink: 0; }
        .feature span { color: rgba(255,255,255,0.7); font-size: 14px; font-weight: 300; }
        .right { display: flex; align-items: center; justify-content: center; padding: 60px 40px; }
        .card { width: 100%; max-width: 420px; animation: fadeUp 0.5s ease both; }
        @keyframes fadeUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
        .card-title { font-family: 'Playfair Display', serif; font-size: 32px; color: var(--ink); margin-bottom: 6px; }
        .card-sub { color: var(--muted); font-size: 14px; margin-bottom: 36px; }
        .tabs { display: flex; background: #ede9e0; border-radius: 10px; padding: 4px; margin-bottom: 32px; }
        .tab {
          flex: 1; padding: 10px; text-align: center; font-size: 14px; font-weight: 500;
          cursor: pointer; border-radius: 8px; border: none; background: transparent;
          color: var(--muted); transition: all 0.2s; font-family: 'DM Sans', sans-serif;
        }
        .tab.active { background: #fff; color: var(--ink); box-shadow: 0 1px 4px rgba(0,0,0,0.1); }
        .field { margin-bottom: 18px; }
        label { display: block; font-size: 12px; font-weight: 500; color: var(--muted); text-transform: uppercase; letter-spacing: 0.8px; margin-bottom: 8px; }
        input {
          width: 100%; padding: 13px 16px; border: 1.5px solid var(--border);
          border-radius: 10px; font-size: 15px; font-family: 'DM Sans', sans-serif;
          background: #fff; color: var(--ink); transition: border-color 0.2s, box-shadow 0.2s; outline: none;
        }
        input:focus { border-color: var(--gold); box-shadow: 0 0 0 3px rgba(201,168,76,0.12); }
        input::placeholder { color: #ccc; }
        .btn {
          width: 100%; padding: 14px; background: var(--ink); color: #fff; border: none;
          border-radius: 10px; font-size: 15px; font-weight: 500; font-family: 'DM Sans', sans-serif;
          cursor: pointer; margin-top: 8px; transition: background 0.2s, transform 0.1s;
        }
        .btn:hover:not(:disabled) { background: #2d2d4e; }
        .btn:active:not(:disabled) { transform: scale(0.99); }
        .btn:disabled { opacity: 0.6; cursor: not-allowed; }
        .btn-inner { display: flex; align-items: center; justify-content: center; gap: 8px; }
        .spinner {
          width: 16px; height: 16px; border: 2px solid rgba(255,255,255,0.3);
          border-top-color: #fff; border-radius: 50%; animation: spin 0.7s linear infinite;
        }
        @keyframes spin { to { transform: rotate(360deg); } }
        .alert { padding: 12px 16px; border-radius: 8px; font-size: 13px; margin-bottom: 18px; }
        .alert.error { background: #fdf0ef; color: var(--error); border: 1px solid #f5c6c2; }
        .alert.success { background: #edfaf3; color: var(--success); border: 1px solid #b7e5cb; }
        @media (max-width: 768px) { .page { grid-template-columns: 1fr; } .left { display: none; } .right { padding: 40px 24px; } }
      `}</style>

      <div className="page">
        <div className="left">
          <div className="brand">
            <div className="brand-icon">💰</div>
            <span className="brand-name">PFMS</span>
          </div>
          <h1 className="left-tagline">Your finances,<br /><span>finally</span> in order.</h1>
          <p className="left-desc">Track spending, set budgets, and reach your savings goals — all in one clean, private space.</p>
          <div className="features">
            {['Track income & expenses in real-time','Smart budget alerts & categories','Savings goals with progress tracking','Monthly reports & insights'].map(f => (
              <div className="feature" key={f}><div className="feature-dot" /><span>{f}</span></div>
            ))}
          </div>
        </div>

        <div className="right">
          <div className="card">
            <h2 className="card-title">{mode === 'login' ? 'Welcome back' : 'Get started'}</h2>
            <p className="card-sub">{mode === 'login' ? 'Sign in to your account' : 'Create your free account'}</p>

            <div className="tabs">
              <button className={`tab ${mode === 'login' ? 'active' : ''}`} onClick={() => { setMode('login'); setError(''); setSuccess('') }}>Sign In</button>
              <button className={`tab ${mode === 'register' ? 'active' : ''}`} onClick={() => { setMode('register'); setError(''); setSuccess('') }}>Register</button>
            </div>

            {error && <div className="alert error">⚠ {error}</div>}
            {success && <div className="alert success">✓ {success}</div>}

            {mode === 'login' ? (
              <form onSubmit={handleLogin}>
                <div className="field">
                  <label>Email</label>
                  <input type="email" placeholder="you@example.com" value={loginForm.email} onChange={e => setLoginForm({...loginForm, email: e.target.value})} required />
                </div>
                <div className="field">
                  <label>Password</label>
                  <input type="password" placeholder="••••••••" value={loginForm.password} onChange={e => setLoginForm({...loginForm, password: e.target.value})} required />
                </div>
                <button className="btn" type="submit" disabled={loading}>
                  <span className="btn-inner">{loading && <span className="spinner" />}{loading ? 'Signing in...' : 'Sign In'}</span>
                </button>
              </form>
            ) : (
              <form onSubmit={handleRegister}>
                <div className="field">
                  <label>Full Name</label>
                  <input type="text" placeholder="John Doe" value={registerForm.name} onChange={e => setRegisterForm({...registerForm, name: e.target.value})} required />
                </div>
                <div className="field">
                  <label>Email</label>
                  <input type="email" placeholder="you@example.com" value={registerForm.email} onChange={e => setRegisterForm({...registerForm, email: e.target.value})} required />
                </div>
                <div className="field">
                  <label>Password</label>
                  <input type="password" placeholder="Min 8 characters" value={registerForm.password} onChange={e => setRegisterForm({...registerForm, password: e.target.value})} required minLength={8} />
                </div>
                <div className="field">
                  <label>Confirm Password</label>
                  <input type="password" placeholder="••••••••" value={registerForm.confirmPassword} onChange={e => setRegisterForm({...registerForm, confirmPassword: e.target.value})} required />
                </div>
                <button className="btn" type="submit" disabled={loading}>
                  <span className="btn-inner">{loading && <span className="spinner" />}{loading ? 'Creating account...' : 'Create Account'}</span>
                </button>
              </form>
            )}
          </div>
        </div>
      </div>
    </>
  )
}
