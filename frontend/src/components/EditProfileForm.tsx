import { useState, type FormEvent } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { updateProfile } from '../api/users'
import { getApiError } from '../api/client'
import type { ProfileResponse } from '../api/types'

interface Props {
  profile: ProfileResponse
  onClose: () => void
}

export default function EditProfileForm({ profile, onClose }: Props) {
  const [displayName, setDisplayName] = useState(profile.displayName)
  const [bio, setBio] = useState(profile.bio ?? '')
  const queryClient = useQueryClient()

  const mutation = useMutation({
    mutationFn: updateProfile,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['profile', profile.username] })
      onClose()
    },
  })

  const apiError = mutation.isError ? getApiError(mutation.error) : null
  const fieldErrors = apiError?.fieldErrors ?? {}

  function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (displayName.trim() === '') return
    // Boş bio backend'de alanı temizler (null yapar) — PATCH semantiği
    mutation.mutate({ displayName, bio })
  }

  const inputClass =
    'mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 focus:outline-none'

  return (
    <form onSubmit={onSubmit} className="mt-4 space-y-3 border-t border-gray-100 pt-4">
      {apiError && (
        <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">
          {apiError.message}
        </p>
      )}
      <div>
        <label htmlFor="displayName" className="block text-sm font-medium text-gray-700">
          Görünen ad
        </label>
        <input
          id="displayName"
          type="text"
          required
          maxLength={60}
          value={displayName}
          onChange={(e) => setDisplayName(e.target.value)}
          className={inputClass}
        />
        {fieldErrors.displayName && (
          <p className="mt-1 text-xs text-red-600">{fieldErrors.displayName}</p>
        )}
      </div>
      <div>
        <label htmlFor="bio" className="block text-sm font-medium text-gray-700">
          Hakkında
        </label>
        <textarea
          id="bio"
          rows={2}
          maxLength={160}
          placeholder="Kendinden bahset (en fazla 160 karakter)"
          value={bio}
          onChange={(e) => setBio(e.target.value)}
          className={`${inputClass} resize-none`}
        />
        {fieldErrors.bio && (
          <p className="mt-1 text-xs text-red-600">{fieldErrors.bio}</p>
        )}
      </div>
      <div className="flex justify-end gap-2">
        <button
          type="button"
          onClick={onClose}
          className="rounded-md border border-gray-300 px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-50"
        >
          Vazgeç
        </button>
        <button
          type="submit"
          disabled={mutation.isPending || displayName.trim() === ''}
          className="rounded-md bg-indigo-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
        >
          {mutation.isPending ? 'Kaydediliyor…' : 'Kaydet'}
        </button>
      </div>
    </form>
  )
}
