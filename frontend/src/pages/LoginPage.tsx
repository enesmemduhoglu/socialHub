import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { login } from '../api/auth'
import { getApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'

export default function LoginPage() {
  const navigate = useNavigate()
  const { isAuthenticated, handleAuthSuccess } = useAuth()
  const [usernameOrEmail, setUsernameOrEmail] = useState('')
  const [password, setPassword] = useState('')

  const mutation = useMutation({
    mutationFn: login,
    onSuccess: (auth) => {
      handleAuthSuccess(auth)
      navigate('/', { replace: true })
    },
  })

  if (isAuthenticated) {
    return <Navigate to="/" replace />
  }

  const apiError = mutation.isError ? getApiError(mutation.error) : null

  function onSubmit(e: FormEvent) {
    e.preventDefault()
    mutation.mutate({ usernameOrEmail, password })
  }

  return (
    <section className="mx-auto max-w-sm rounded-lg border border-gray-200 bg-white p-6">
      <h1 className="text-lg font-semibold text-gray-900">Giriş Yap</h1>

      {apiError && (
        <p className="mt-3 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">
          {apiError.status === 429
            ? 'Çok fazla deneme yaptınız, lütfen biraz bekleyin.'
            : apiError.message}
        </p>
      )}

      <form onSubmit={onSubmit} className="mt-4 space-y-4">
        <div>
          <label
            htmlFor="usernameOrEmail"
            className="block text-sm font-medium text-gray-700"
          >
            Kullanıcı adı veya e-posta
          </label>
          <input
            id="usernameOrEmail"
            type="text"
            required
            autoComplete="username"
            value={usernameOrEmail}
            onChange={(e) => setUsernameOrEmail(e.target.value)}
            className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 focus:outline-none"
          />
        </div>
        <div>
          <label
            htmlFor="password"
            className="block text-sm font-medium text-gray-700"
          >
            Parola
          </label>
          <input
            id="password"
            type="password"
            required
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 focus:outline-none"
          />
        </div>
        <button
          type="submit"
          disabled={mutation.isPending}
          className="w-full rounded-md bg-indigo-600 px-3 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
        >
          {mutation.isPending ? 'Giriş yapılıyor…' : 'Giriş Yap'}
        </button>
      </form>

      <p className="mt-4 text-center text-sm text-gray-600">
        Hesabın yok mu?{' '}
        <Link to="/register" className="font-medium text-indigo-600 hover:underline">
          Kayıt ol
        </Link>
      </p>
    </section>
  )
}
