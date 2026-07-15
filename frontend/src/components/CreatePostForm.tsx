import { useState, type FormEvent } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createPost } from '../api/posts'
import { getApiError } from '../api/client'

const MAX_LENGTH = 1000

export default function CreatePostForm() {
  const [content, setContent] = useState('')
  const queryClient = useQueryClient()

  const mutation = useMutation({
    mutationFn: createPost,
    onSuccess: () => {
      setContent('')
      queryClient.invalidateQueries({ queryKey: ['feed'] })
    },
  })

  const apiError = mutation.isError ? getApiError(mutation.error) : null
  const trimmed = content.trim()

  function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (trimmed === '') return
    mutation.mutate({ content: trimmed })
  }

  return (
    <form
      onSubmit={onSubmit}
      className="rounded-lg border border-gray-200 bg-white p-4"
    >
      <label htmlFor="new-post" className="sr-only">
        Yeni gönderi
      </label>
      <textarea
        id="new-post"
        rows={3}
        maxLength={MAX_LENGTH}
        placeholder="Neler oluyor?"
        value={content}
        onChange={(e) => setContent(e.target.value)}
        className="w-full resize-none rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 focus:outline-none"
      />
      {apiError && (
        <p className="mt-2 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">
          {apiError.message}
        </p>
      )}
      <div className="mt-2 flex items-center justify-between">
        <span className="text-xs text-gray-400">
          {content.length}/{MAX_LENGTH}
        </span>
        <button
          type="submit"
          disabled={mutation.isPending || trimmed === ''}
          className="rounded-md bg-indigo-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
        >
          {mutation.isPending ? 'Paylaşılıyor…' : 'Paylaş'}
        </button>
      </div>
    </form>
  )
}
