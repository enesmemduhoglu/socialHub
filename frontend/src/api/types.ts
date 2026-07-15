// Backend DTO'larının TypeScript karşılıkları.
// Tarihler ISO-8601 string (UTC Instant), id'ler number (Java Long).

export type Role = 'USER' | 'ADMIN'

export interface UserResponse {
  id: number
  username: string
  email: string
  displayName: string
  bio: string | null
  role: Role
  createdAt: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  displayName?: string
}

export interface LoginRequest {
  usernameOrEmail: string
  password: string
}

export interface AuthResponse {
  accessToken: string
  tokenType: string
  expiresAt: string
  user: UserResponse
}

export interface UpdateProfileRequest {
  // PATCH semantiği: alan gönderilmezse/null ise değişmez, boş string alanı temizler (null yapar)
  displayName?: string | null
  bio?: string | null
}

export interface AuthorSummary {
  id: number
  username: string
  displayName: string
}

export type UserSummary = AuthorSummary

export interface PostResponse {
  id: number
  content: string
  author: AuthorSummary
  createdAt: string
  updatedAt: string
  likeCount: number
  commentCount: number
  likedByCurrentUser: boolean
}

export interface CreatePostRequest {
  content: string
}

export type UpdatePostRequest = CreatePostRequest

export interface ProfileResponse {
  id: number
  username: string
  displayName: string
  bio: string | null
  followerCount: number
  followingCount: number
  followedByCurrentUser: boolean
}

export interface CommentResponse {
  id: number
  content: string
  author: AuthorSummary
  createdAt: string
  updatedAt: string
}

export interface CreateCommentRequest {
  content: string
}

export type NotificationType = 'FOLLOW' | 'LIKE' | 'COMMENT'

export interface NotificationResponse {
  id: number
  type: NotificationType
  actor: UserSummary
  postId: number | null // FOLLOW bildirimlerinde null
  read: boolean
  createdAt: string
}

export interface UnreadCountResponse {
  unreadCount: number
}

export interface CursorPageResponse<T> {
  items: T[]
  nextCursor: number | null
  hasMore: boolean
}

export interface ApiError {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
  fieldErrors?: Record<string, string>
}
