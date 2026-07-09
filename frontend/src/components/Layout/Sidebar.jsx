import { NavLink } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import {
  LayoutDashboard, Users, Megaphone, Tag, BarChart3,
  Settings, ClipboardList, Sparkles, ChevronRight, Zap
} from 'lucide-react'
import { clsx } from 'clsx'

const navItems = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/customers', icon: Users, label: 'Customers' },
  { to: '/campaigns', icon: Megaphone, label: 'Campaigns' },
  { to: '/campaigns/ai', icon: Sparkles, label: 'AI Generator', badge: 'AI' },
  { to: '/coupons', icon: Tag, label: 'Coupons' },
  { to: '/analytics', icon: BarChart3, label: 'Analytics' },
  { to: '/audit-logs', icon: ClipboardList, label: 'Audit Logs', adminOnly: true },
  { to: '/settings', icon: Settings, label: 'Settings' },
]

export default function Sidebar() {
  const { user } = useAuth()

  return (
    <aside className="w-64 bg-gray-900 text-white flex flex-col min-h-screen">
      {/* Logo */}
      <div className="p-6 border-b border-gray-700">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 bg-gradient-to-br from-primary-500 to-indigo-600 rounded-xl flex items-center justify-center shadow-lg">
            <Zap className="w-5 h-5 text-white" />
          </div>
          <div>
            <h1 className="text-lg font-bold text-white tracking-tight">LoyaltyPro</h1>
            <p className="text-xs text-gray-400">Marketing Platform</p>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
        {navItems.map(({ to, icon: Icon, label, badge, adminOnly }) => {
          if (adminOnly && user?.role !== 'ROLE_ADMIN') return null
          return (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) => clsx(
                'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-150 group',
                isActive
                  ? 'bg-primary-600 text-white shadow-md'
                  : 'text-gray-400 hover:text-white hover:bg-gray-800'
              )}
            >
              <Icon className="w-5 h-5 flex-shrink-0" />
              <span className="flex-1">{label}</span>
              {badge && (
                <span className="px-1.5 py-0.5 text-xs font-bold rounded bg-indigo-500 text-white">
                  {badge}
                </span>
              )}
            </NavLink>
          )
        })}
      </nav>

      {/* User Footer */}
      <div className="p-4 border-t border-gray-700">
        <div className="flex items-center gap-3 px-2">
          <div className="w-8 h-8 rounded-full bg-gradient-to-br from-primary-500 to-indigo-500 flex items-center justify-center text-sm font-bold text-white">
            {user?.name?.charAt(0)?.toUpperCase()}
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-white truncate">{user?.name}</p>
            <p className="text-xs text-gray-400 truncate">
              {user?.role?.replace('ROLE_', '').replace('_', ' ')}
            </p>
          </div>
          <ChevronRight className="w-4 h-4 text-gray-500" />
        </div>
      </div>
    </aside>
  )
}
