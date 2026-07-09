import { useState, useEffect } from 'react'
import { Search, Filter, ShieldCheck, Download } from 'lucide-react'
import auditService from '../services/auditService'
import Pagination from '../components/UI/Pagination'
import LoadingSpinner from '../components/UI/LoadingSpinner'
import toast from 'react-hot-toast'
import { format } from 'date-fns'

const ACTIONS = [
  '', 'USER_LOGIN', 'USER_LOGOUT', 'USER_REGISTERED',
  'CUSTOMER_CREATED', 'CUSTOMER_UPDATED', 'CUSTOMER_DELETED',
  'TIER_UPGRADED', 'POINTS_EARNED', 'POINTS_REDEEMED',
  'CAMPAIGN_CREATED', 'CAMPAIGN_PUBLISHED', 'CAMPAIGN_CANCELLED',
  'COUPON_CREATED', 'COUPON_REDEEMED',
]

const ACTION_COLORS = {
  USER_LOGIN: 'bg-blue-50 text-blue-700',
  USER_LOGOUT: 'bg-gray-100 text-gray-600',
  USER_REGISTERED: 'bg-green-50 text-green-700',
  CUSTOMER_CREATED: 'bg-emerald-50 text-emerald-700',
  CUSTOMER_UPDATED: 'bg-yellow-50 text-yellow-700',
  CUSTOMER_DELETED: 'bg-red-50 text-red-700',
  TIER_UPGRADED: 'bg-purple-50 text-purple-700',
  POINTS_EARNED: 'bg-green-50 text-green-700',
  POINTS_REDEEMED: 'bg-indigo-50 text-indigo-700',
  CAMPAIGN_CREATED: 'bg-blue-50 text-blue-700',
  CAMPAIGN_PUBLISHED: 'bg-green-50 text-green-700',
  CAMPAIGN_CANCELLED: 'bg-red-50 text-red-700',
  COUPON_CREATED: 'bg-amber-50 text-amber-700',
  COUPON_REDEEMED: 'bg-orange-50 text-orange-700',
}

export default function AuditLogsPage() {
  const [logs, setLogs] = useState([])
  const [loading, setLoading] = useState(true)
  const [pagination, setPagination] = useState({ page: 0, totalPages: 0, totalElements: 0 })
  const [filters, setFilters] = useState({ action: '', entityType: '', dateFrom: '', dateTo: '' })
  const [showFilters, setShowFilters] = useState(false)

  const fetchLogs = async (page = 0) => {
    setLoading(true)
    try {
      const params = {
        page, size: 50,
        ...(filters.action && { action: filters.action }),
        ...(filters.entityType && { entityType: filters.entityType }),
        ...(filters.dateFrom && { dateFrom: filters.dateFrom }),
        ...(filters.dateTo && { dateTo: filters.dateTo }),
      }
      const data = await auditService.getLogs(params)
      setLogs(data.content || [])
      setPagination({ page: data.number, totalPages: data.totalPages, totalElements: data.totalElements })
    } catch {
      toast.error('Failed to load audit logs')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchLogs(0) }, [filters])

  const handleFilterChange = (key, val) => {
    setFilters(p => ({ ...p, [key]: val }))
  }

  const clearFilters = () => setFilters({ action: '', entityType: '', dateFrom: '', dateTo: '' })

  const hasFilters = Object.values(filters).some(Boolean)

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <div className="flex items-center gap-2 mb-0.5">
            <ShieldCheck className="w-6 h-6 text-indigo-600" />
            <h1 className="text-2xl font-bold text-gray-900">Audit Logs</h1>
          </div>
          <p className="text-gray-500 text-sm">{pagination.totalElements.toLocaleString()} total events</p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => setShowFilters(p => !p)}
            className={`btn-secondary ${hasFilters ? 'border-indigo-300 text-indigo-700 bg-indigo-50' : ''}`}
          >
            <Filter className="w-4 h-4" />
            Filters
            {hasFilters && <span className="w-2 h-2 rounded-full bg-indigo-500" />}
          </button>
        </div>
      </div>

      {/* Filters Panel */}
      {showFilters && (
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-4 animate-slide-down">
          <div className="grid md:grid-cols-4 gap-4">
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1.5">Action</label>
              <select value={filters.action}
                onChange={e => handleFilterChange('action', e.target.value)}
                className="input-field text-sm py-2">
                {ACTIONS.map(a => <option key={a} value={a}>{a || 'All Actions'}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1.5">Entity Type</label>
              <input type="text" value={filters.entityType}
                onChange={e => handleFilterChange('entityType', e.target.value)}
                className="input-field text-sm py-2" placeholder="e.g. Customer, Campaign" />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1.5">From</label>
              <input type="datetime-local" value={filters.dateFrom}
                onChange={e => handleFilterChange('dateFrom', e.target.value)}
                className="input-field text-sm py-2" />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1.5">To</label>
              <input type="datetime-local" value={filters.dateTo}
                onChange={e => handleFilterChange('dateTo', e.target.value)}
                className="input-field text-sm py-2" />
            </div>
          </div>
          {hasFilters && (
            <button onClick={clearFilters} className="mt-3 text-xs text-gray-500 hover:text-red-600 transition-colors">
              Clear all filters
            </button>
          )}
        </div>
      )}

      {/* Logs Table */}
      <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
        {loading ? (
          <LoadingSpinner text="Loading audit logs..." />
        ) : logs.length === 0 ? (
          <div className="py-20 text-center">
            <ShieldCheck className="w-12 h-12 text-gray-300 mx-auto mb-3" />
            <p className="text-gray-500">No audit logs found</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-100 bg-gray-50/50">
                  <th className="text-left py-3 px-4 font-medium text-gray-500 text-xs uppercase tracking-wide">Timestamp</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-500 text-xs uppercase tracking-wide">Action</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-500 text-xs uppercase tracking-wide">Entity</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-500 text-xs uppercase tracking-wide">Description</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-500 text-xs uppercase tracking-wide">User</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-500 text-xs uppercase tracking-wide">IP</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {logs.map(log => (
                  <tr key={log.id} className="hover:bg-gray-50/50 transition-colors group">
                    <td className="py-3 px-4 text-gray-500 whitespace-nowrap text-xs font-mono">
                      {log.createdAt ? format(new Date(log.createdAt), 'MMM dd HH:mm:ss') : '-'}
                    </td>
                    <td className="py-3 px-4">
                      <span className={`inline-block px-2 py-0.5 rounded-full text-xs font-medium ${ACTION_COLORS[log.action] || 'bg-gray-100 text-gray-600'}`}>
                        {log.action}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-gray-700">
                      <div className="flex items-center gap-1.5">
                        <span className="font-medium">{log.entityType || '-'}</span>
                        {log.entityId && <span className="text-xs text-gray-400">#{log.entityId}</span>}
                      </div>
                    </td>
                    <td className="py-3 px-4 text-gray-600 max-w-xs truncate">{log.description || '-'}</td>
                    <td className="py-3 px-4">
                      {log.userEmail ? (
                        <div className="flex items-center gap-1.5">
                          <div className="w-5 h-5 rounded-full bg-indigo-100 flex items-center justify-center text-xs font-bold text-indigo-600">
                            {log.userEmail[0].toUpperCase()}
                          </div>
                          <span className="text-gray-600 text-xs">{log.userEmail}</span>
                        </div>
                      ) : (
                        <span className="text-gray-400 text-xs">System</span>
                      )}
                    </td>
                    <td className="py-3 px-4 text-gray-400 text-xs font-mono">{log.ipAddress || '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <Pagination
        page={pagination.page}
        totalPages={pagination.totalPages}
        onPageChange={fetchLogs}
      />
    </div>
  )
}
