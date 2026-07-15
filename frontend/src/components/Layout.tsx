import { Link, Outlet, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getUnreadCount } from '../api/notifications'
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
                <NotificationBell />
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

function NotificationBell() {
  // 30 sn'de bir okunmamış sayısını tazele (basit polling)
  const { data } = useQuery({
    queryKey: ['unreadCount'],
    queryFn: getUnreadCount,
    refetchInterval: 30_000,
  })

  const unread = data?.unreadCount ?? 0

  return (
    <Link
      to="/notifications"
      aria-label={
        unread > 0 ? `Bildirimler (${unread} okunmamış)` : 'Bildirimler'
      }
      className="relative text-gray-500 hover:text-indigo-600"
    >
      <svg
        viewBox="0 0 24 24"
        className="h-5 w-5"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
      >
        <path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9" />
        <path d="M13.7 21a2 2 0 0 1-3.4 0" />
      </svg>
      {unread > 0 && (
        <span className="absolute -top-1.5 -right-2 flex h-4 min-w-4 items-center justify-center rounded-full bg-rose-600 px-1 text-[10px] font-bold text-white">
          {unread > 99 ? '99+' : unread}
        </span>
      )}
    </Link>
  )
}
