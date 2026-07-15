import { Link, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function onLogout() {
    logout()
    navigate('/login')
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="border-b border-gray-200 bg-white">
        <div className="mx-auto flex max-w-2xl items-center justify-between px-4 py-3">
          <Link to="/" className="text-xl font-bold text-indigo-600">
            SocialHub
          </Link>
          <nav className="flex items-center gap-4 text-sm text-gray-600">
            {user ? (
              <>
                <Link
                  to={`/users/${user.username}`}
                  className="font-medium text-gray-900 hover:text-indigo-600"
                >
                  @{user.username}
                </Link>
                <button
                  type="button"
                  onClick={onLogout}
                  className="hover:text-indigo-600"
                >
                  Çıkış
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="hover:text-indigo-600">
                  Giriş
                </Link>
                <Link to="/register" className="hover:text-indigo-600">
                  Kayıt
                </Link>
              </>
            )}
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-2xl px-4 py-6">
        <Outlet />
      </main>
    </div>
  )
}
