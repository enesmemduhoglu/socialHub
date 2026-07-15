import { api } from './client'
import type {
  CreatePostRequest,
  CursorPageResponse,
  PostResponse,
} from './types'

export async function getFeed(
  cursor?: number,
): Promise<CursorPageResponse<PostResponse>> {
  const { data } = await api.get<CursorPageResponse<PostResponse>>('/feed', {
    params: { cursor },
  })
  return data
}

export async function createPost(
  request: CreatePostRequest,
): Promise<PostResponse> {
  const { data } = await api.post<PostResponse>('/posts', request)
  return data
}

export async function getPost(id: number): Promise<PostResponse> {
  const { data } = await api.get<PostResponse>(`/posts/${id}`)
  return data
}

export async function likePost(id: number): Promise<void> {
  await api.post(`/posts/${id}/like`)
}

export async function unlikePost(id: number): Promise<void> {
  await api.delete(`/posts/${id}/like`)
}
