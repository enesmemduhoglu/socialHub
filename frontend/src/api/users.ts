import { api } from './client'
import type {
  CursorPageResponse,
  PostResponse,
  ProfileResponse,
  UpdateProfileRequest,
  UserResponse,
  UserSummary,
} from './types'

export async function getProfile(username: string): Promise<ProfileResponse> {
  const { data } = await api.get<ProfileResponse>(`/users/${username}`)
  return data
}

export async function updateProfile(
  request: UpdateProfileRequest,
): Promise<UserResponse> {
  const { data } = await api.patch<UserResponse>('/users/me', request)
  return data
}

export async function followUser(username: string): Promise<void> {
  await api.post(`/users/${username}/follow`)
}

export async function unfollowUser(username: string): Promise<void> {
  await api.delete(`/users/${username}/follow`)
}

export async function getUserPosts(
  username: string,
  cursor?: number,
): Promise<CursorPageResponse<PostResponse>> {
  const { data } = await api.get<CursorPageResponse<PostResponse>>(
    `/users/${username}/posts`,
    { params: { cursor } },
  )
  return data
}

export async function getFollowers(
  username: string,
  cursor?: number,
): Promise<CursorPageResponse<UserSummary>> {
  const { data } = await api.get<CursorPageResponse<UserSummary>>(
    `/users/${username}/followers`,
    { params: { cursor } },
  )
  return data
}

export async function getFollowing(
  username: string,
  cursor?: number,
): Promise<CursorPageResponse<UserSummary>> {
  const { data } = await api.get<CursorPageResponse<UserSummary>>(
    `/users/${username}/following`,
    { params: { cursor } },
  )
  return data
}
