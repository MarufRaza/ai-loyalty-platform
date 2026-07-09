import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Plus, Megaphone, Sparkles, Play, X, Filter } from 'lucide-react'
import campaignService from '../services/campaignService'
import Badge from '../components/UI/Badge'
import Pagination from '../components/UI/Pagination'
import LoadingSpinner from '../components/UI/LoadingSpinner'
import toast from 'react-hot-toast'

const STATUSES = ['', 'DRAFT', 'SCHEDULED', 'RUNNING', 'COMPLETED', 'CANCELLED']

export default function CampaignsPage() {
  const [campaigns, setCampaigns] = useState([])
  const [loading, setLoading] = useState(true)
  const [pagination, setPagination] = useState({ page: 0, totalPages: 0, totalElements: 0 })
  const [statusFilter, setStatusFilter] = useState('')

  const fetchCampaigns = async (page = 0) => {
    setLoading(true)
    try {
      const data = await campaignService.getAll({
        page, size: 20,
        ...(statusFilter && { status: statusFilter })
      })
      setCampaigns(data.content || [])
      setPagination({ page: data.number, totalPages: data.totalPages, totalElements: data.totalElements })
    } catch {
      toast.error('Failed to load campaigns')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchCampaigns(0) }, [statusFilter])

  const handlePublish = async (id) => {
    try {
      await campaignService.publish(id)
      toast.success('Campaign published!')
      fetchCampaigns(pagination.page)
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Failed to publish')
    }
  }

  const handleCancel = async (id) => {
    if (!confirm('Cancel this campaign?')) return
    try {
      await campaignService.cancel(id)
      toast.success('Campaign cancelled')
      fetchCampaigns(pagination.page)
    } catch {
      toast.error('Failed to cancel campaign')
    }
  }

  const typeIcons = {
    EMAIL: '📧', WHATSAPP: '💬', SMS: '📱', PUSH_NOTIFICATION: '🔔'
  }

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Campaigns</h1>
          <p className="text-gray-500 text-sm mt-0.5">{pagination.totalElements} total campaigns</p>
        </div>
        <div className="flex items-center gap-3">
          <Link to="/campaigns/ai" className="btn-secondary">
            <Sparkles className="w-4 h-4 text-indigo-600" />
            AI Generate
          </Link>
          <Link to="/campaigns/new" className="btn-primary">
            <Plus className="w-4 h-4" />
            New Campaign
          </Link>
        </div>
      </div>

      {/* Status Filter */}
      <div className="flex items-center gap-2 flex-wrap">
        {STATUSES.map(s => (
          <button
            key={s}
            onClick={() => setStatusFilter(s)}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
              statusFilter === s
                ? 'bg-primary-600 text-white'
                : 'bg-white text-gray-600 border border-gray-200 hover:bg-gray-50'
            }`}
          >
            {s || 'All'}
          </button>
        ))}
      </div>

      {/* Campaigns Grid */}
      {loading ? (
        <LoadingSpinner text="Loading campaigns..." />
      ) : campaigns.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-200 py-20 text-center">
          <Megaphone className="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <p className="text-gray-500 font-medium">No campaigns found</p>
          <p className="text-gray-400 text-sm mt-1">Create your first campaign to get started</p>
          <Link to="/campaigns/new" className="btn-primary mt-4 inline-flex">
            <Plus className="w-4 h-4" /> Create Campaign
          </Link>
        </div>
      ) : (
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
          {campaigns.map((c) => (
            <div key={c.id} className="bg-white rounded-xl border border-gray-200 shadow-sm hover:shadow-md transition-all duration-200 overflow-hidden group">
              <div className="p-5">
                <div className="flex items-start justify-between mb-3">
                  <div className="flex items-center gap-2">
                    <span className="text-lg">{typeIcons[c.campaignType] || '📣'}</span>
                    <div>
                      <p className="text-xs text-gray-400 uppercase tracking-wide">{c.campaignType}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-1.5">
                    <Badge status={c.status} label={c.status} />
                    {c.aiGenerated && (
                      <span className="inline-flex items-center gap-0.5 px-1.5 py-0.5 bg-indigo-50 text-indigo-600 text-xs rounded-full font-medium">
                        <Sparkles className="w-3 h-3" /> AI
                      </span>
                    )}
                  </div>
                </div>

                <h3 className="font-semibold text-gray-900 mb-1 line-clamp-1">{c.name}</h3>
                <p className="text-sm text-gray-500 line-clamp-2 mb-3">{c.objective}</p>

                {c.segmentName && (
                  <div className="text-xs text-gray-400 mb-3">
                    Segment: <span className="font-medium text-gray-600">{c.segmentName}</span>
                  </div>
                )}

                {c.analytics && (
                  <div className="grid grid-cols-3 gap-2 py-3 border-t border-gray-100 text-center">
                    <div>
                      <p className="text-sm font-bold text-gray-900">{c.analytics.totalSent}</p>
                      <p className="text-xs text-gray-400">Sent</p>
                    </div>
                    <div>
                      <p className="text-sm font-bold text-gray-900">{c.analytics.totalOpened}</p>
                      <p className="text-xs text-gray-400">Opened</p>
                    </div>
                    <div>
                      <p className="text-sm font-bold text-gray-900">{c.analytics.totalConverted}</p>
                      <p className="text-xs text-gray-400">Converted</p>
                    </div>
                  </div>
                )}
              </div>

              <div className="px-5 pb-4 flex gap-2">
                {c.status === 'DRAFT' && (
                  <button onClick={() => handlePublish(c.id)}
                    className="flex-1 flex items-center justify-center gap-1.5 py-2 bg-green-600 hover:bg-green-700 text-white text-xs font-medium rounded-lg transition-colors">
                    <Play className="w-3 h-3" /> Publish
                  </button>
                )}
                {(c.status === 'RUNNING' || c.status === 'SCHEDULED') && (
                  <button onClick={() => handleCancel(c.id)}
                    className="flex-1 flex items-center justify-center gap-1.5 py-2 bg-red-50 hover:bg-red-100 text-red-700 text-xs font-medium rounded-lg transition-colors">
                    <X className="w-3 h-3" /> Cancel
                  </button>
                )}
                <Link to={`/campaigns/${c.id}/edit`}
                  className="flex-1 flex items-center justify-center py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 text-xs font-medium rounded-lg transition-colors">
                  View Details
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}

      <Pagination
        page={pagination.page}
        totalPages={pagination.totalPages}
        onPageChange={fetchCampaigns}
      />
    </div>
  )
}
