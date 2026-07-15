import { Link } from 'react-router-dom'
import type { UserSummary } from '../api/types'

export default function UserSummaryRow({ user }: { user: UserSummary }) {
  return (
    <Link
      to={`/users/${user.username}`}
      className="flex items-baseline gap-2 rounded-lg border border-gray-200 bg-white p-3 text-sm hover:border-indigo-300"
    >
      <span className="font-semibold text-gray-900">{user.displayName}</span>
      <span className="text-gray-500">@{user.username}</span>
    </Link>
  )
}
