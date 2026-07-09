import { clsx } from 'clsx'

const tiers = {
  SILVER: 'badge-silver',
  GOLD: 'badge-gold',
  PLATINUM: 'badge-platinum',
}

const statusColors = {
  DRAFT: 'bg-gray-100 text-gray-700',
  SCHEDULED: 'bg-blue-100 text-blue-700',
  RUNNING: 'bg-green-100 text-green-700',
  COMPLETED: 'bg-indigo-100 text-indigo-700',
  CANCELLED: 'bg-red-100 text-red-700',
  PAUSED: 'bg-yellow-100 text-yellow-700',
  ACTIVE: 'bg-green-100 text-green-700',
  INACTIVE: 'bg-gray-100 text-gray-600',
}

export default function Badge({ label, tier, status, className }) {
  const colorClass = tier ? tiers[tier] : statusColors[status] || 'bg-gray-100 text-gray-700'

  return (
    <span className={clsx(
      'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
      colorClass,
      className
    )}>
      {tier && <span className="w-1.5 h-1.5 rounded-full mr-1.5 bg-current opacity-70" />}
      {label || tier || status}
    </span>
  )
}
