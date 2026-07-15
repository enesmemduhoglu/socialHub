import axios from 'axios'
import type { ApiError } from './types'

export const TOKEN_STORAGE_KEY = 'accessToken'

export const api = axios.create({
  baseURL: '/api',
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_STORAGE_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Süresi dolmuş/geçersiz token: oturumu kapatıp girişe yönlendir.
    // /auth/* hariç — login'de yanlış parola da 401 döner, yönlendirme döngüsü olmasın.
    if (
      axios.isAxiosError(error) &&
      error.response?.status === 401 &&
      !error.config?.url?.startsWith('/auth/')
    ) {
      localStorage.removeItem(TOKEN_STORAGE_KEY)
      if (window.location.pathname !== '/login') {
        window.location.assign('/login')
      }
    }
    return Promise.reject(error)
  },
)

export function getApiError(error: unknown): ApiError | null {
  if (axios.isAxiosError(error) && error.response?.data) {
    return error.response.data as ApiError
  }
  return null
}
