import { useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getPost } from '../api/posts'
import { createComment, deleteComment, getComments } from '../api/comments'
import { getApiError } from '../api/client'
import type { CommentResponse } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { useInfiniteCursor } from '../lib/useInfiniteCursor'
import { patchPostInCaches } from '../lib/postCache'
import { timeAgo } from '../lib/time'
import PostCard from '../components/PostCard'

const MAX_COMMENT_LENGTH = 500

export default function PostDetailPage() {
  const { id = '' } = useParams()
  const postId = Number(id)

  const {
    data: post,
    isPending,
    isError,
    error,
  } = useQuery({
    queryKey: ['post', postId],
    queryFn: () => getPost(postId),
    enabled: Number.isFinite(postId),
  })

  if (!Number.isFinite(postId)) {
    return (
      <p className="py-6 text-center text-sm text-gray-500">
        Geçersiz gönderi adresi.
      </p>
    )
  }

  if (isPending) {
    return (
      <p className="py-6 text-center text-sm text-gray-500">
        Gönderi yükleniyor…
      </p>
    )
  }

  if (isError) {
    const notFound =
      (error as { response?: { status?: number } })?.response?.status === 404
    return (
      <p className="py-6 text-center text-sm text-gray-500">
        {notFound ? 'Böyle bir gönderi yok.' : 'Gönderi yüklenemedi.'}
      </p>
    )
  }

  return (
    <div className="space-y-4">
      <PostCard post={post} />
      <CommentSection postId={postId} />
    </div>
  )
}

function CommentSection({ postId }: { postId: number }) {
  const {
    items: comments,
    sentinelRef,
    isPending,
    isError,
    isFetchingNextPage,
  } = useInfiniteCursor(['comments', postId], (cursor) =>
    getComments(postId, cursor),
  )

  return (
    <section className="space-y-3">
      <h2 className="text-sm font-semibold text-gray-700">Yorumlar</h2>
      <CommentForm postId={postId} />

      {isPending && (
        <p className="py-4 text-center text-sm text-gray-500">Yükleniyor…</p>
      )}
      {isError && (
        <p className="py-4 text-center text-sm text-red-600">
          Yorumlar yüklenemedi.
        </p>
      )}
      {!isPending && !isError && comments.length === 0 && (
        <p className="py-4 text-center text-sm text-gray-500">
          İlk yorumu sen yap!
        </p>
      )}

      {comments.map((comment) => (
        <CommentRow key={comment.id} postId={postId} comment={comment} />
      ))}
      <div ref={sentinelRef} />
      {isFetchingNextPage && (
        <p className="py-3 text-center text-sm text-gray-500">Yükleniyor…</p>
      )}
    </section>
  )
}

function CommentForm({ postId }: { postId: number }) {
  const [content, setContent] = useState('')
  const queryClient = useQueryClient()

  const mutation = useMutation({
    mutationFn: () => createComment(postId, { content: content.trim() }),
    onSuccess: () => {
      setContent('')
      queryClient.invalidateQueries({ queryKey: ['comments', postId] })
      patchPostInCaches(queryClient, postId, (p) => ({
        ...p,
        commentCount: p.commentCount + 1,
      }))
    },
  })

  const apiError = mutation.isError ? getApiError(mutation.error) : null
  const trimmed = content.trim()

  function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (trimmed === '') return
    mutation.mutate()
  }

  return (
    <form
      onSubmit={onSubmit}
      className="rounded-lg border border-gray-200 bg-white p-3"
    >
      <label htmlFor="new-comment" className="sr-only">
        Yorum yaz
      </label>
      <textarea
        id="new-comment"
        rows={2}
        maxLength={MAX_COMMENT_LENGTH}
        placeholder="Yorumunu yaz…"
        value={content}
        onChange={(e) => setContent(e.target.value)}
        className="w-full resize-none rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 focus:outline-none"
      />
      {apiError && (
        <p className="mt-2 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">
          {apiError.message}
        </p>
      )}
      <div className="mt-2 flex items-center justify-between">
        <span className="text-xs text-gray-400">
          {content.length}/{MAX_COMMENT_LENGTH}
        </span>
        <button
          type="submit"
          disabled={mutation.isPending || trimmed === ''}
          className="rounded-md bg-indigo-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
        >
          {mutation.isPending ? 'Gönderiliyor…' : 'Yorum Yap'}
        </button>
      </div>
    </form>
  )
}

function CommentRow({
  postId,
  comment,
}: {
  postId: number
  comment: CommentResponse
}) {
  const { user } = useAuth()
  const queryClient = useQueryClient()
  const isOwn = user?.username === comment.author.username

  const deleteMutation = useMutation({
    mutationFn: () => deleteComment(postId, comment.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments', postId] })
      patchPostInCaches(queryClient, postId, (p) => ({
        ...p,
        commentCount: Math.max(0, p.commentCount - 1),
      }))
    },
  })

  return (
    <article className="rounded-lg border border-gray-200 bg-white p-3">
      <header className="flex items-baseline gap-2 text-sm">
        <Link
          to={`/users/${comment.author.username}`}
          className="font-semibold text-gray-900 hover:underline"
        >
          {comment.author.displayName}
        </Link>
        <span className="text-gray-500">@{comment.author.username}</span>
        <span className="text-gray-400">·</span>
        <time dateTime={comment.createdAt} className="text-gray-500">
          {timeAgo(comment.createdAt)}
        </time>
        {isOwn && (
          <button
            type="button"
            onClick={() => deleteMutation.mutate()}
            disabled={deleteMutation.isPending}
            className="ml-auto text-xs text-gray-400 hover:text-red-600 disabled:opacity-50"
          >
            Sil
          </button>
        )}
      </header>
      <p className="mt-1 text-sm whitespace-pre-wrap break-words text-gray-800">
        {comment.content}
      </p>
    </article>
  )
}
