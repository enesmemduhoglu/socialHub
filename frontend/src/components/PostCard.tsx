import { Link } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { likePost, unlikePost } from '../api/posts'
import type { PostResponse } from '../api/types'
import { timeAgo } from '../lib/time'
import { invalidatePostCaches, patchPostInCaches } from '../lib/postCache'

export default function PostCard({ post }: { post: PostResponse }) {
  const queryClient = useQueryClient()

  const likeMutation = useMutation({
    mutationFn: () =>
      post.likedByCurrentUser ? unlikePost(post.id) : likePost(post.id),
    // Optimistic: sunucu yanıtını beklemeden kalbi ve sayacı güncelle
    onMutate: () => {
      patchPostInCaches(queryClient, post.id, (p) => ({
        ...p,
        likedByCurrentUser: !p.likedByCurrentUser,
        likeCount: p.likeCount + (p.likedByCurrentUser ? -1 : 1),
      }))
    },
    onError: () => invalidatePostCaches(queryClient),
  })

  return (
    <article className="rounded-lg border border-gray-200 bg-white p-4">
      <header className="flex items-baseline gap-2 text-sm">
        <Link
          to={`/users/${post.author.username}`}
          className="font-semibold text-gray-900 hover:underline"
        >
          {post.author.displayName}
        </Link>
        <Link
          to={`/users/${post.author.username}`}
          className="text-gray-500 hover:underline"
        >
          @{post.author.username}
        </Link>
        <span className="text-gray-400">·</span>
        <time dateTime={post.createdAt} className="text-gray-500">
          {timeAgo(post.createdAt)}
        </time>
      </header>

      <p className="mt-2 text-sm whitespace-pre-wrap break-words text-gray-800">
        {post.content}
      </p>

      <footer className="mt-3 flex items-center gap-6 text-sm">
        <button
          type="button"
          onClick={() => likeMutation.mutate()}
          aria-label={post.likedByCurrentUser ? 'Beğeniyi geri al' : 'Beğen'}
          className={`flex items-center gap-1.5 ${
            post.likedByCurrentUser
              ? 'text-rose-600'
              : 'text-gray-500 hover:text-rose-600'
          }`}
        >
          <svg
            viewBox="0 0 24 24"
            className="h-4 w-4"
            fill={post.likedByCurrentUser ? 'currentColor' : 'none'}
            stroke="currentColor"
            strokeWidth="2"
          >
            <path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.6l-1-1a5.5 5.5 0 0 0-7.8 7.8l1 1L12 21.2l7.8-7.8 1-1a5.5 5.5 0 0 0 0-7.8z" />
          </svg>
          {post.likeCount}
        </button>

        <Link
          to={`/posts/${post.id}`}
          className="flex items-center gap-1.5 text-gray-500 hover:text-indigo-600"
        >
          <svg
            viewBox="0 0 24 24"
            className="h-4 w-4"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
          >
            <path d="M21 11.5a8.4 8.4 0 0 1-8.5 8.3 8.7 8.7 0 0 1-3.9-.9L3 20l1.1-5.2a8 8 0 0 1-.9-3.7A8.4 8.4 0 0 1 11.7 3a8.4 8.4 0 0 1 9.3 8.5z" />
          </svg>
          {post.commentCount}
        </Link>
      </footer>
    </article>
  )
}
