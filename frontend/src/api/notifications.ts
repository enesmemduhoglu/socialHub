import { api } from './client'
import type {
  CursorPageResponse,
  NotificationResponse,
  UnreadCountResponse,
} from './types'

export async function getNotifications(
  cursor?: number,
): Promise<CursorPageResponse<NotificationResponse>> {
  const { data } = await api.get<CursorPageResponse<NotificationResponse>>(
    '/notifications',
    { params: { cursor } },
  )
  return data
}

export async function getUnreadCount(): Promise<UnreadCountResponse> {
  const { data } = await api.get<UnreadCountResponse>(
    '/notifications/unread-count',
  )
  return data
}

export async function markNotificationRead(id: number): Promise<void> {
  await api.post(`/notifications/${id}/read`)
}

export async function markAllNotificationsRead(): Promise<void> {
  await api.post('/notifications/read-all')
}
