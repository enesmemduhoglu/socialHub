import { useEffect, useRef } from 'react'
import { useInfiniteQuery } from '@tanstack/react-query'
import { getFeed } from '../api/posts'
import CreatePostForm from '../components/CreatePostForm'
import PostCard from '../components/PostCard'

export default function FeedPage() {
  const {
    data,
    isPending,
    isError,
    refetch,
    hasNextPage,
    isFetchingNextPage,
    fetchNextPage,
  } = useInfiniteQuery({
    queryKey: ['feed'],
    queryFn: ({ pageParam }) => getFeed(pageParam),
    initialPageParam: undefined as number | undefined,
    getNextPageParam: (last) =>
      last.hasMore && last.nextCursor !== null ? last.nextCursor : undefined,
  })

  // Sayfa sonundaki görünmez işaretçi görünür olunca sonraki sayfayı çek
  const sentinelRef = useRef<HTMLDivElement | null>(null)
  useEffect(() => {
    const sentinel = sentinelRef.current
    if (!sentinel) return
    const observer = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting && hasNextPage && !isFetchingNextPage) {
        fetchNextPage()
      }
    })
    observer.observe(sentinel)
    return () => observer.disconnect()
  }, [hasNextPage, isFetchingNextPage, fetchNextPage])

  const posts = data?.pages.flatMap((page) => page.items) ?? []

  return (
    <div className="space-y-4">
      <CreatePostForm />

      {isPending && (
        <p className="py-6 text-center text-sm text-gray-500">
          Akış yükleniyor…
        </p>
      )}

      {isError && (
        <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-center">
          <p className="text-sm text-red-700">Akış yüklenemedi.</p>
          <button
            type="button"
            onClick={() => refetch()}
            className="mt-2 rounded-md bg-red-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-red-700"
          >
            Tekrar dene
          </button>
        </div>
      )}

      {!isPending && !isError && posts.length === 0 && (
        <p className="py-6 text-center text-sm text-gray-500">
          Akışın boş. Bir şeyler paylaş veya birilerini takip et!
        </p>
      )}

      {posts.map((post) => (
        <PostCard key={post.id} post={post} />
      ))}

      <div ref={sentinelRef} />

      {isFetchingNextPage && (
        <p className="py-4 text-center text-sm text-gray-500">Yükleniyor…</p>
      )}
      {!isPending && !isError && posts.length > 0 && !hasNextPage && (
        <p className="py-4 text-center text-xs text-gray-400">
          Akışın sonuna geldin.
        </p>
      )}
    </div>
  )
}
