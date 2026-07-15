import type { InfiniteData, QueryClient } from '@tanstack/react-query'
import type { CursorPageResponse, PostResponse } from '../api/types'

/**
 * Bir gönderiyi, bulunduğu TÜM cache'lerde yerinde günceller:
 * sonsuz listeler (feed, kullanıcı gönderileri) + tekil detay.
 * Beğeni gibi anlık geri bildirim isteyen mutasyonların optimistic
 * güncellemesi için kullanılır.
 */
export function patchPostInCaches(
  queryClient: QueryClient,
  postId: number,
  patch: (post: PostResponse) => PostResponse,
) {
  queryClient.setQueriesData<InfiniteData<CursorPageResponse<PostResponse>>>(
    {
      predicate: (query) =>
        query.queryKey[0] === 'feed' || query.queryKey[0] === 'userPosts',
    },
    (data) =>
      data && {
        ...data,
        pages: data.pages.map((page) => ({
          ...page,
          items: page.items.map((post) =>
            post.id === postId ? patch(post) : post,
          ),
        })),
      },
  )
  queryClient.setQueryData<PostResponse>(['post', postId], (post) =>
    post ? patch(post) : post,
  )
}

/** Optimistic güncelleme ters giderse gerçek veriyi geri çek. */
export function invalidatePostCaches(queryClient: QueryClient) {
  queryClient.invalidateQueries({ queryKey: ['feed'] })
  queryClient.invalidateQueries({ queryKey: ['userPosts'] })
  queryClient.invalidateQueries({ queryKey: ['post'] })
}
