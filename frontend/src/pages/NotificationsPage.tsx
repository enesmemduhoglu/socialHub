import { useNavigate } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import {
  getNotifications,
  markAllNotificationsRead,
  markNotificationRead,
} from '../api/notifications'
import type { NotificationResponse } from '../api/types'
import { useInfiniteCursor } from '../lib/useInfiniteCursor'
import { timeAgo } from '../lib/time'

function notificationText(type: NotificationResponse['type']): string {
  switch (type) {
    case 'FOLLOW':
      return 'seni takip etti'
    case 'LIKE':
      return 'gönderini beğendi'
    case 'COMMENT':
      return 'gönderine yorum yaptı'
  }
}

export default function NotificationsPage() {
  const {
    items: notifications,
    sentinelRef,
    isPending,
    isError,
    isFetchingNextPage,
  } = useInfiniteCursor(['notifications'], getNotifications)

  const queryClient = useQueryClient()

  const readAllMutation = useMutation({
    mutationFn: markAllNotificationsRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] })
      queryClient.invalidateQueries({ queryKey: ['unreadCount'] })
    },
  })

  const hasUnread = notifications.some((n) => !n.read)

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-lg font-semibold text-gray-900">Bildirimler</h1>
        {hasUnread && (
          <button
            type="button"
            onClick={() => readAllMutation.mutate()}
            disabled={readAllMutation.isPending}
            className="text-sm font-medium text-indigo-600 hover:underline disabled:opacity-50"
          >
            Tümünü okundu işaretle
          </button>
        )}
      </div>

      {isPending && (
        <p className="py-6 text-center text-sm text-gray-500">Yükleniyor…</p>
      )}
      {isError && (
        <p className="py-6 text-center text-sm text-red-600">
          Bildirimler yüklenemedi.
        </p>
      )}
      {!isPending && !isError && notifications.length === 0 && (
        <p className="py-6 text-center text-sm text-gray-500">
          Henüz bildirimin yok.
        </p>
      )}

      <div className="space-y-2">
        {notifications.map((notification) => (
          <NotificationRow key={notification.id} notification={notification} />
        ))}
      </div>
      <div ref={sentinelRef} />
      {isFetchingNextPage && (
        <p className="py-3 text-center text-sm text-gray-500">Yükleniyor…</p>
      )}
    </div>
  )
}

function NotificationRow({
  notification,
}: {
  notification: NotificationResponse
}) {
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const readMutation = useMutation({
    mutationFn: () => markNotificationRead(notification.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] })
      queryClient.invalidateQueries({ queryKey: ['unreadCount'] })
    },
  })

  function onClick() {
    if (!notification.read) {
      readMutation.mutate()
    }
    // FOLLOW'da postId null'dır — aktörün profiline git; diğerlerinde gönderiye
    if (notification.postId !== null) {
      navigate(`/posts/${notification.postId}`)
    } else {
      navigate(`/users/${notification.actor.username}`)
    }
  }

  return (
    <button
      type="button"
      onClick={onClick}
      className={`block w-full rounded-lg border p-3 text-left text-sm ${
        notification.read
          ? 'border-gray-200 bg-white'
          : 'border-indigo-200 bg-indigo-50'
      } hover:border-indigo-400`}
    >
      <span className="font-semibold text-gray-900">
        {notification.actor.displayName}
      </span>{' '}
      <span className="text-gray-500">@{notification.actor.username}</span>{' '}
      <span className="text-gray-800">
        {notificationText(notification.type)}
      </span>
      <span className="mx-1 text-gray-400">·</span>
      <time dateTime={notification.createdAt} className="text-gray-500">
        {timeAgo(notification.createdAt)}
      </time>
    </button>
  )
}
