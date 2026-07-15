import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from 'react'
import { getMe } from '../api/auth'
import { TOKEN_STORAGE_KEY } from '../api/client'
import type { AuthResponse, UserResponse } from '../api/types'

interface AuthContextValue {
  user: UserResponse | null
  isAuthenticated: boolean
  isLoading: boolean
  handleAuthSuccess: (auth: AuthResponse) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserResponse | null>(null)
  // Sayfa yenilendiğinde token varsa /users/me ile oturumu geri yükle
  const [isLoading, setIsLoading] = useState(
    () => localStorage.getItem(TOKEN_STORAGE_KEY) !== null,
  )

  useEffect(() => {
    if (!localStorage.getItem(TOKEN_STORAGE_KEY)) return
    let cancelled = false
    getMe()
      .then((me) => {
        if (!cancelled) setUser(me)
      })
      .catch(() => {
        // 401 durumunu client interceptor'ı temizliyor; diğer hatalarda oturumsuz devam
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  const handleAuthSuccess = useCallback((auth: AuthResponse) => {
    localStorage.setItem(TOKEN_STORAGE_KEY, auth.accessToken)
    setUser(auth.user)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_STORAGE_KEY)
    setUser(null)
  }, [])

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: user !== null,
        isLoading,
        handleAuthSuccess,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth, AuthProvider içinde kullanılmalı')
  }
  return ctx
}
