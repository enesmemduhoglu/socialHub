import { Link } from 'react-router-dom'
import type { PostResponse } from '../api/types'
import { timeAgo } from '../lib/time'

export default function PostCard({ post }: { post: PostResponse }) {
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
    </article>
  )
}
