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
