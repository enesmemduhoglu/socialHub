import { useEffect, useRef } from 'react'
import { useInfiniteQuery } from '@tanstack/react-query'
import type { CursorPageResponse } from '../api/types'

/**
 * Backend'in keyset pagination kontratı (items/nextCursor/hasMore) için
 * useInfiniteQuery + IntersectionObserver'lı sonsuz kaydırma.
 * Dönen sentinelRef listenin sonuna boş bir div olarak yerleştirilir.
 */
export function useInfiniteCursor<T>(
  queryKey: readonly unknown[],
  fetchPage: (cursor?: number) => Promise<CursorPageResponse<T>>,
) {
  const query = useInfiniteQuery({
    queryKey,
    queryFn: ({ pageParam }) => fetchPage(pageParam),
    initialPageParam: undefined as number | undefined,
    getNextPageParam: (last) =>
      last.hasMore && last.nextCursor !== null ? last.nextCursor : undefined,
  })

  const { hasNextPage, isFetchingNextPage, fetchNextPage } = query
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

  const items = query.data?.pages.flatMap((page) => page.items) ?? []

  return { ...query, items, sentinelRef }
}
