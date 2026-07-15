import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  followUser,
  getFollowers,
  getFollowing,
  getProfile,
  getUserPosts,
  unfollowUser,
} from '../api/users'
import { useAuth } from '../auth/AuthContext'
import { useInfiniteCursor } from '../lib/useInfiniteCursor'
import EditProfileForm from '../components/EditProfileForm'
import PostCard from '../components/PostCard'
import UserSummaryRow from '../components/UserSummaryRow'

type Tab = 'posts' | 'followers' | 'following'

export default function ProfilePage() {
  const { username = '' } = useParams()
  const { user: currentUser } = useAuth()
  const [tab, setTab] = useState<Tab>('posts')
  const [editing, setEditing] = useState(false)
  const queryClient = useQueryClient()

  // Başka bir profile geçildiğinde sekme/düzenleme durumunu sıfırla
  useEffect(() => {
    setTab('posts')
    setEditing(false)
  }, [username])

  const {
    data: profile,
    isPending,
    isError,
    error,
  } = useQuery({
    queryKey: ['profile', username],
    queryFn: () => getProfile(username),
  })

  const followMutation = useMutation({
    mutationFn: (following: boolean) =>
      following ? unfollowUser(username) : followUser(username),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['profile', username] })
      queryClient.invalidateQueries({ queryKey: ['followList', username] })
      // Takip değişince ana akışın içeriği de değişir
      queryClient.invalidateQueries({ queryKey: ['feed'] })
    },
  })

  if (isPending) {
    return (
      <p className="py-6 text-center text-sm text-gray-500">
        Profil yükleniyor…
      </p>
    )
  }

  if (isError) {
    const notFound =
      (error as { response?: { status?: number } })?.response?.status === 404
    return (
      <p className="py-6 text-center text-sm text-gray-500">
        {notFound ? 'Böyle bir kullanıcı yok.' : 'Profil yüklenemedi.'}
      </p>
    )
  }

  const isOwnProfile = currentUser?.username === profile.username

  const tabClass = (t: Tab) =>
    `border-b-2 px-3 py-2 text-sm font-medium ${
      tab === t
        ? 'border-indigo-600 text-indigo-600'
        : 'border-transparent text-gray-500 hover:text-gray-700'
    }`

  return (
    <div className="space-y-4">
      <section className="rounded-lg border border-gray-200 bg-white p-6">
        <div className="flex items-start justify-between gap-4">
          <div>
            <h1 className="text-xl font-bold text-gray-900">
              {profile.displayName}
            </h1>
            <p className="text-sm text-gray-500">@{profile.username}</p>
          </div>
          {isOwnProfile ? (
            <button
              type="button"
              onClick={() => setEditing((v) => !v)}
              className="rounded-md border border-gray-300 px-4 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
              Profili Düzenle
            </button>
          ) : (
            <button
              type="button"
              disabled={followMutation.isPending}
              onClick={() => followMutation.mutate(profile.followedByCurrentUser)}
              className={
                profile.followedByCurrentUser
                  ? 'rounded-md border border-gray-300 px-4 py-1.5 text-sm font-medium text-gray-700 hover:border-red-300 hover:text-red-600 disabled:opacity-50'
                  : 'rounded-md bg-indigo-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50'
              }
            >
              {profile.followedByCurrentUser ? 'Takibi Bırak' : 'Takip Et'}
            </button>
          )}
        </div>

        {profile.bio && (
          <p className="mt-3 text-sm whitespace-pre-wrap break-words text-gray-800">
            {profile.bio}
          </p>
        )}

        <p className="mt-3 text-sm text-gray-600">
          <span className="font-semibold text-gray-900">
            {profile.followerCount}
          </span>{' '}
          takipçi
          <span className="mx-2 text-gray-300">·</span>
          <span className="font-semibold text-gray-900">
            {profile.followingCount}
          </span>{' '}
          takip
        </p>

        {isOwnProfile && editing && (
          <EditProfileForm profile={profile} onClose={() => setEditing(false)} />
        )}
      </section>

      <nav className="flex border-b border-gray-200">
        <button type="button" onClick={() => setTab('posts')} className={tabClass('posts')}>
          Gönderiler
        </button>
        <button
          type="button"
          onClick={() => setTab('followers')}
          className={tabClass('followers')}
        >
          Takipçiler
        </button>
        <button
          type="button"
          onClick={() => setTab('following')}
          className={tabClass('following')}
        >
          Takip Edilenler
        </button>
      </nav>

      {tab === 'posts' && <UserPostsTab username={username} />}
      {tab === 'followers' && <FollowListTab username={username} kind="followers" />}
      {tab === 'following' && <FollowListTab username={username} kind="following" />}
    </div>
  )
}

function UserPostsTab({ username }: { username: string }) {
  const {
    items: posts,
    sentinelRef,
    isPending,
    isError,
    isFetchingNextPage,
  } = useInfiniteCursor(['userPosts', username], (cursor) =>
    getUserPosts(username, cursor),
  )

  return (
    <div className="space-y-4">
      {isPending && (
        <p className="py-6 text-center text-sm text-gray-500">Yükleniyor…</p>
      )}
      {isError && (
        <p className="py-6 text-center text-sm text-red-600">
          Gönderiler yüklenemedi.
        </p>
      )}
      {!isPending && !isError && posts.length === 0 && (
        <p className="py-6 text-center text-sm text-gray-500">
          Henüz gönderi yok.
        </p>
      )}
      {posts.map((post) => (
        <PostCard key={post.id} post={post} />
      ))}
      <div ref={sentinelRef} />
      {isFetchingNextPage && (
        <p className="py-4 text-center text-sm text-gray-500">Yükleniyor…</p>
      )}
    </div>
  )
}

function FollowListTab({
  username,
  kind,
}: {
  username: string
  kind: 'followers' | 'following'
}) {
  const fetchPage =
    kind === 'followers'
      ? (cursor?: number) => getFollowers(username, cursor)
      : (cursor?: number) => getFollowing(username, cursor)

  const {
    items: users,
    sentinelRef,
    isPending,
    isError,
    isFetchingNextPage,
  } = useInfiniteCursor(['followList', username, kind], fetchPage)

  return (
    <div className="space-y-2">
      {isPending && (
        <p className="py-6 text-center text-sm text-gray-500">Yükleniyor…</p>
      )}
      {isError && (
        <p className="py-6 text-center text-sm text-red-600">
          Liste yüklenemedi.
        </p>
      )}
      {!isPending && !isError && users.length === 0 && (
        <p className="py-6 text-center text-sm text-gray-500">
          {kind === 'followers'
            ? 'Henüz takipçisi yok.'
            : 'Henüz kimseyi takip etmiyor.'}
        </p>
      )}
      {users.map((user) => (
        <UserSummaryRow key={user.id} user={user} />
      ))}
      <div ref={sentinelRef} />
      {isFetchingNextPage && (
        <p className="py-4 text-center text-sm text-gray-500">Yükleniyor…</p>
      )}
    </div>
  )
}
