import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { register } from '../api/auth'
import { getApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'

export default function RegisterPage() {
  const navigate = useNavigate()
  const { isAuthenticated, handleAuthSuccess } = useAuth()
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [displayName, setDisplayName] = useState('')

  const mutation = useMutation({
    mutationFn: register,
    onSuccess: (auth) => {
      handleAuthSuccess(auth)
      navigate('/', { replace: true })
    },
  })

  if (isAuthenticated) {
    return <Navigate to="/" replace />
  }

  const apiError = mutation.isError ? getApiError(mutation.error) : null
  const fieldErrors = apiError?.fieldErrors ?? {}

  function onSubmit(e: FormEvent) {
    e.preventDefault()
    mutation.mutate({
      username,
      email,
      password,
      displayName: displayName.trim() === '' ? undefined : displayName,
    })
  }

  const inputClass =
    'mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 focus:outline-none'

  return (
    <section className="mx-auto max-w-sm rounded-lg border border-gray-200 bg-white p-6">
      <h1 className="text-lg font-semibold text-gray-900">Kayıt Ol</h1>

      {apiError && (
        <p className="mt-3 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">
          {apiError.status === 429
            ? 'Çok fazla deneme yaptınız, lütfen biraz bekleyin.'
            : apiError.message}
        </p>
      )}

      <form onSubmit={onSubmit} className="mt-4 space-y-4">
        <div>
          <label htmlFor="username" className="block text-sm font-medium text-gray-700">
            Kullanıcı adı
          </label>
          <input
            id="username"
            type="text"
            required
            minLength={3}
            maxLength={30}
            pattern="[a-zA-Z0-9_]+"
            title="Sadece harf, rakam ve alt çizgi"
            autoComplete="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className={inputClass}
          />
          {fieldErrors.username && (
            <p className="mt-1 text-xs text-red-600">{fieldErrors.username}</p>
          )}
        </div>
        <div>
          <label htmlFor="email" className="block text-sm font-medium text-gray-700">
            E-posta
          </label>
          <input
            id="email"
            type="email"
            required
            maxLength={255}
            autoComplete="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className={inputClass}
          />
          {fieldErrors.email && (
            <p className="mt-1 text-xs text-red-600">{fieldErrors.email}</p>
          )}
        </div>
        <div>
          <label htmlFor="password" className="block text-sm font-medium text-gray-700">
            Parola
          </label>
          <input
            id="password"
            type="password"
            required
            minLength={8}
            maxLength={72}
            autoComplete="new-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className={inputClass}
          />
          {fieldErrors.password && (
            <p className="mt-1 text-xs text-red-600">{fieldErrors.password}</p>
          )}
        </div>
        <div>
          <label htmlFor="displayName" className="block text-sm font-medium text-gray-700">
            Görünen ad <span className="text-gray-400">(isteğe bağlı)</span>
          </label>
          <input
            id="displayName"
            type="text"
            maxLength={60}
            value={displayName}
            onChange={(e) => setDisplayName(e.target.value)}
            className={inputClass}
          />
          {fieldErrors.displayName && (
            <p className="mt-1 text-xs text-red-600">{fieldErrors.displayName}</p>
          )}
        </div>
        <button
          type="submit"
          disabled={mutation.isPending}
          className="w-full rounded-md bg-indigo-600 px-3 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
        >
          {mutation.isPending ? 'Kayıt yapılıyor…' : 'Kayıt Ol'}
        </button>
      </form>

      <p className="mt-4 text-center text-sm text-gray-600">
        Zaten hesabın var mı?{' '}
        <Link to="/login" className="font-medium text-indigo-600 hover:underline">
          Giriş yap
        </Link>
      </p>
    </section>
  )
}
