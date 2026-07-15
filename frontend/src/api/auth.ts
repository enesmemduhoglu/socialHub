import { api } from './client'
import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  UserResponse,
} from './types'

export async function login(request: LoginRequest): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>('/auth/login', request)
  return data
}

export async function register(request: RegisterRequest): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>('/auth/register', request)
  return data
}

export async function getMe(): Promise<UserResponse> {
  const { data } = await api.get<UserResponse>('/users/me')
  return data
}
