import type { PostResponse } from '../api/types'
import { timeAgo } from '../lib/time'

export default function PostCard({ post }: { post: PostResponse }) {
  return (
    <article className="rounded-lg border border-gray-200 bg-white p-4">
      <header className="flex items-baseline gap-2 text-sm">
        <span className="font-semibold text-gray-900">
          {post.author.displayName}
        </span>
        <span className="text-gray-500">@{post.author.username}</span>
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
