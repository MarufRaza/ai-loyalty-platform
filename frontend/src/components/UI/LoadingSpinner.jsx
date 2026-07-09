import { clsx } from 'clsx'
import { Loader2 } from 'lucide-react'

export default function LoadingSpinner({ size = 'md', fullScreen = false, text = '' }) {
  const sizes = { sm: 'w-4 h-4', md: 'w-8 h-8', lg: 'w-12 h-12' }

  if (fullScreen) {
    return (
      <div className="fixed inset-0 bg-white flex items-center justify-center z-50">
        <div className="flex flex-col items-center gap-4">
          <Loader2 className="w-12 h-12 text-primary-600 animate-spin" />
          <p className="text-gray-500 text-sm font-medium">Loading...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex items-center justify-center gap-2 py-8">
      <Loader2 className={clsx(sizes[size], 'text-primary-600 animate-spin')} />
      {text && <span className="text-gray-500 text-sm">{text}</span>}
    </div>
  )
}
