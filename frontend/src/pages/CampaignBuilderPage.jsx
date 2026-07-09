import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Save, Loader2 } from 'lucide-react'
import campaignService from '../services/campaignService'
import toast from 'react-hot-toast'

const CAMPAIGN_TYPES = ['EMAIL', 'WHATSAPP', 'SMS', 'PUSH_NOTIFICATION']
const STATUSES = ['DRAFT', 'SCHEDULED']

export default function CampaignBuilderPage() {
  const navigate = useNavigate()
  const { id } = useParams()
  const isEdit = !!id

  const [form, setForm] = useState({
    name: '', objective: '', campaignType: 'EMAIL', status: 'DRAFT',
    subjectLine: '', content: '', callToAction: '', segmentId: '',
    scheduledAt: '', aiGenerated: false
  })
  const [segments, setSegments] = useState([])
  const [loading, setLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    campaignService.getSegments().then(setSegments).catch(() => {})

    if (isEdit) {
      setLoading(true)
      campaignService.getById(id)
        .then(c => setForm({
          name: c.name, objective: c.objective, campaignType: c.campaignType,
          status: c.status, subjectLine: c.subjectLine || '', content: c.content || '',
          callToAction: c.callToAction || '', segmentId: c.segmentId || '',
          scheduledAt: c.scheduledAt || '', aiGenerated: c.aiGenerated
        }))
        .catch(() => toast.error('Failed to load campaign'))
        .finally(() => setLoading(false))
    }
  }, [id, isEdit])

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.name || !form.objective || !form.content) {
      toast.error('Please fill in all required fields')
      return
    }
    setSubmitting(true)
    try {
      const payload = {
        ...form,
        segmentId: form.segmentId || null,
        scheduledAt: form.scheduledAt || null,
      }
      if (isEdit) {
        await campaignService.update(id, payload)
        toast.success('Campaign updated!')
      } else {
        await campaignService.create(payload)
        toast.success('Campaign created!')
      }
      navigate('/campaigns')
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Failed to save campaign')
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <div className="flex items-center justify-center py-16"><Loader2 className="w-8 h-8 animate-spin text-primary-600" /></div>

  return (
    <div className="max-w-4xl mx-auto space-y-6 animate-fade-in">
      <button onClick={() => navigate('/campaigns')}
        className="inline-flex items-center gap-2 text-sm text-gray-500 hover:text-gray-900 transition-colors">
        <ArrowLeft className="w-4 h-4" /> Back to Campaigns
      </button>

      <div>
        <h1 className="text-2xl font-bold text-gray-900">{isEdit ? 'Edit Campaign' : 'Create Campaign'}</h1>
        <p className="text-gray-500 text-sm mt-0.5">Fill in the details below to configure your campaign</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Basic Info */}
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 space-y-5">
          <h2 className="text-base font-semibold text-gray-900 border-b border-gray-100 pb-3">Campaign Details</h2>

          <div className="grid md:grid-cols-2 gap-5">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">
                Campaign Name <span className="text-red-500">*</span>
              </label>
              <input type="text" value={form.name}
                onChange={e => setForm(p => ({ ...p, name: e.target.value }))}
                className="input-field" placeholder="e.g. Summer Sale 2024" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">
                Campaign Type <span className="text-red-500">*</span>
              </label>
              <select value={form.campaignType}
                onChange={e => setForm(p => ({ ...p, campaignType: e.target.value }))}
                className="input-field">
                {CAMPAIGN_TYPES.map(t => <option key={t} value={t}>{t.replace('_', ' ')}</option>)}
              </select>
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">
              Objective <span className="text-red-500">*</span>
            </label>
            <input type="text" value={form.objective}
              onChange={e => setForm(p => ({ ...p, objective: e.target.value }))}
              className="input-field" placeholder="What do you want to achieve with this campaign?" />
          </div>

          <div className="grid md:grid-cols-2 gap-5">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Target Segment</label>
              <select value={form.segmentId}
                onChange={e => setForm(p => ({ ...p, segmentId: e.target.value }))}
                className="input-field">
                <option value="">All Customers</option>
                {segments.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Status</label>
              <select value={form.status}
                onChange={e => setForm(p => ({ ...p, status: e.target.value }))}
                className="input-field">
                {STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
              </select>
            </div>
          </div>

          {form.status === 'SCHEDULED' && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Schedule Date & Time</label>
              <input type="datetime-local" value={form.scheduledAt}
                onChange={e => setForm(p => ({ ...p, scheduledAt: e.target.value }))}
                className="input-field" />
            </div>
          )}
        </div>

        {/* Content */}
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 space-y-5">
          <h2 className="text-base font-semibold text-gray-900 border-b border-gray-100 pb-3">Campaign Content</h2>

          {form.campaignType === 'EMAIL' && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Subject Line</label>
              <input type="text" value={form.subjectLine}
                onChange={e => setForm(p => ({ ...p, subjectLine: e.target.value }))}
                className="input-field" placeholder="Compelling email subject..." />
            </div>
          )}

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">
              Message Content <span className="text-red-500">*</span>
            </label>
            <textarea rows={8} value={form.content}
              onChange={e => setForm(p => ({ ...p, content: e.target.value }))}
              className="input-field resize-none"
              placeholder="Write your campaign message here..." />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Call to Action</label>
            <input type="text" value={form.callToAction}
              onChange={e => setForm(p => ({ ...p, callToAction: e.target.value }))}
              className="input-field" placeholder="e.g. Shop Now, Claim Offer, Learn More" />
          </div>
        </div>

        <div className="flex justify-end gap-3">
          <button type="button" onClick={() => navigate('/campaigns')} className="btn-secondary">
            Cancel
          </button>
          <button type="submit" disabled={submitting} className="btn-primary">
            {submitting ? <Loader2 className="w-4 h-4 animate-spin" /> : <Save className="w-4 h-4" />}
            {isEdit ? 'Save Changes' : 'Create Campaign'}
          </button>
        </div>
      </form>
    </div>
  )
}
