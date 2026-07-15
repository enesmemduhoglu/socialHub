import { api } from './client'
import type {
  CommentResponse,
  CreateCommentRequest,
  CursorPageResponse,
} from './types'

export async function getComments(
  postId: number,
  cursor?: number,
): Promise<CursorPageResponse<CommentResponse>> {
  const { data } = await api.get<CursorPageResponse<CommentResponse>>(
    `/posts/${postId}/comments`,
    { params: { cursor } },
  )
  return data
}

export async function createComment(
  postId: number,
  request: CreateCommentRequest,
): Promise<CommentResponse> {
  const { data } = await api.post<CommentResponse>(
    `/posts/${postId}/comments`,
    request,
  )
  return data
}

export async function deleteComment(
  postId: number,
  commentId: number,
): Promise<void> {
  await api.delete(`/posts/${postId}/comments/${commentId}`)
}
