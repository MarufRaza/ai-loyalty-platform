import { useState, useEffect } from 'react'
import { Plus, Tag, Loader2, Clock, BadgeCheck, XCircle } from 'lucide-react'
import couponService from '../services/couponService'
import Badge from '../components/UI/Badge'
import Modal from '../components/UI/Modal'
import Pagination from '../components/UI/Pagination'
import LoadingSpinner from '../components/UI/LoadingSpinner'
import toast from 'react-hot-toast'
import { format, isPast } from 'date-fns'

const COUPON_TYPES = [
  { value: 'FLAT_DISCOUNT', label: 'Flat Discount ($)' },
  { value: 'PERCENTAGE_DISCOUNT', label: 'Percentage Discount (%)' },
  { value: 'FREE_SHIPPING', label: 'Free Shipping' },
]

export default function CouponsPage() {
  const [coupons, setCoupons] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [pagination, setPagination] = useState({ page: 0, totalPages: 0, totalElements: 0 })
  const [form, setForm] = useState({
    description: '', couponType: 'FLAT_DISCOUNT', discountValue: '',
    discountPercentage: '', minimumOrderAmount: '', maximumDiscountAmount: '',
    expiryDate: '', usageLimit: 1
  })

  const fetchCoupons = async (page = 0) => {
    setLoading(true)
    try {
      const data = await couponService.getAll({ page, size: 20 })
      setCoupons(data.content || [])
      setPagination({ page: data.number, totalPages: data.totalPages, totalElements: data.totalElements })
    } catch {
      toast.error('Failed to load coupons')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchCoupons() }, [])

  const handleCreate = async (e) => {
    e.preventDefault()
    if (!form.description || !form.expiryDate) {
      toast.error('Please fill in required fields')
      return
    }
    setSubmitting(true)
    try {
      const payload = {
        ...form,
        discountValue: form.couponType === 'FLAT_DISCOUNT' ? parseFloat(form.discountValue) : null,
        discountPercentage: form.couponType === 'PERCENTAGE_DISCOUNT' ? parseFloat(form.discountPercentage) : null,
        minimumOrderAmount: form.minimumOrderAmount ? parseFloat(form.minimumOrderAmount) : null,
        maximumDiscountAmount: form.maximumDiscountAmount ? parseFloat(form.maximumDiscountAmount) : null,
        usageLimit: parseInt(form.usageLimit),
        expiryDate: new Date(form.expiryDate).toISOString(),
      }
      await couponService.create(payload)
      toast.success('Coupon created!')
      setShowModal(false)
      setForm({ description: '', couponType: 'FLAT_DISCOUNT', discountValue: '',
        discountPercentage: '', minimumOrderAmount: '', maximumDiscountAmount: '',
        expiryDate: '', usageLimit: 1 })
      fetchCoupons()
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Failed to create coupon')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDeactivate = async (id) => {
    if (!confirm('Deactivate this coupon?')) return
    try {
      await couponService.deactivate(id)
      toast.success('Coupon deactivated')
      fetchCoupons()
    } catch {
      toast.error('Failed to deactivate')
    }
  }

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Coupons</h1>
          <p className="text-gray-500 text-sm mt-0.5">{pagination.totalElements} total coupons</p>
        </div>
        <button onClick={() => setShowModal(true)} className="btn-primary">
          <Plus className="w-4 h-4" />
          Create Coupon
        </button>
      </div>

      {loading ? (
        <LoadingSpinner text="Loading coupons..." />
      ) : coupons.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-200 py-20 text-center">
          <Tag className="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <p className="text-gray-500 font-medium">No coupons yet</p>
          <button onClick={() => setShowModal(true)} className="btn-primary mt-4">
            <Plus className="w-4 h-4" /> Create First Coupon
          </button>
        </div>
      ) : (
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
          {coupons.map(c => {
            const expired = isPast(new Date(c.expiryDate))
            const exhausted = c.usageCount >= c.usageLimit

            return (
              <div key={c.id} className={`bg-white rounded-xl border shadow-sm overflow-hidden ${
                  expired || !c.active ? 'border-gray-200 opacity-60' : 'border-gray-200 hover:shadow-md'
              } transition-all duration-200`}>
                {/* Code Banner */}
                <div className={`px-5 py-3 flex items-center justify-between ${
                  expired || !c.active ? 'bg-gray-100' : 'bg-gradient-to-r from-primary-600 to-indigo-600'
                }`}>
                  <code className={`text-lg font-bold tracking-widest ${
                    expired || !c.active ? 'text-gray-500' : 'text-white'
                  }`}>{c.code}</code>
                  {expired ? (
                    <XCircle className="w-5 h-5 text-gray-400" />
                  ) : exhausted ? (
                    <BadgeCheck className="w-5 h-5 text-white opacity-70" />
                  ) : c.active ? (
                    <BadgeCheck className="w-5 h-5 text-white" />
                  ) : (
                    <XCircle className="w-5 h-5 text-gray-400" />
                  )}
                </div>

                <div className="p-4 space-y-3">
                  <p className="text-sm text-gray-700 font-medium">{c.description}</p>

                  <div className="flex items-center justify-between">
                    <div className="text-xl font-bold text-gray-900">
                      {c.couponType === 'FLAT_DISCOUNT' && `$${c.discountValue} OFF`}
                      {c.couponType === 'PERCENTAGE_DISCOUNT' && `${c.discountPercentage}% OFF`}
                      {c.couponType === 'FREE_SHIPPING' && 'FREE SHIPPING'}
                    </div>
                    <span className="text-xs text-gray-400">{c.couponType.replace('_', ' ')}</span>
                  </div>

                  <div className="space-y-1.5 text-xs text-gray-500">
                    {c.minimumOrderAmount && <p>Min order: ${c.minimumOrderAmount}</p>}
                    <div className="flex items-center gap-1">
                      <Clock className="w-3 h-3" />
                      Expires: {new Date(c.expiryDate).toLocaleDateString()}
                      {expired && <span className="text-red-500 font-medium ml-1">(EXPIRED)</span>}
                    </div>
                    <div className="flex items-center justify-between">
                      <span>Used: {c.usageCount}/{c.usageLimit}</span>
                      <div className="w-20 h-1.5 bg-gray-100 rounded-full overflow-hidden">
                        <div
                          className="h-full bg-primary-500 rounded-full"
                          style={{ width: `${Math.min(100, (c.usageCount / c.usageLimit) * 100)}%` }}
                        />
                      </div>
                    </div>
                  </div>

                  {c.active && !expired && (
                    <button
                      onClick={() => handleDeactivate(c.id)}
                      className="w-full py-1.5 text-xs text-red-600 bg-red-50 hover:bg-red-100 rounded-lg font-medium transition-colors"
                    >
                      Deactivate
                    </button>
                  )}
                </div>
              </div>
            )
          })}
        </div>
      )}

      <Pagination page={pagination.page} totalPages={pagination.totalPages} onPageChange={fetchCoupons} />

      {/* Create Modal */}
      <Modal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
        title="Create New Coupon"
        size="lg"
        footer={
          <>
            <button onClick={() => setShowModal(false)} className="btn-secondary">Cancel</button>
            <button onClick={handleCreate} disabled={submitting} className="btn-primary">
              {submitting ? <Loader2 className="w-4 h-4 animate-spin" /> : <Tag className="w-4 h-4" />}
              Create Coupon
            </button>
          </>
        }
      >
        <form className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Description *</label>
            <input type="text" value={form.description}
              onChange={e => setForm(p => ({ ...p, description: e.target.value }))}
              className="input-field" placeholder="e.g. Summer Sale 20% Off" />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Coupon Type *</label>
              <select value={form.couponType}
                onChange={e => setForm(p => ({ ...p, couponType: e.target.value }))}
                className="input-field">
                {COUPON_TYPES.map(t => <option key={t.value} value={t.value}>{t.label}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Usage Limit</label>
              <input type="number" min="1" value={form.usageLimit}
                onChange={e => setForm(p => ({ ...p, usageLimit: e.target.value }))}
                className="input-field" />
            </div>
          </div>

          {form.couponType === 'FLAT_DISCOUNT' && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Discount Amount ($)</label>
              <input type="number" step="0.01" min="0.01" value={form.discountValue}
                onChange={e => setForm(p => ({ ...p, discountValue: e.target.value }))}
                className="input-field" placeholder="e.g. 10.00" />
            </div>
          )}
          {form.couponType === 'PERCENTAGE_DISCOUNT' && (
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Discount % *</label>
                <input type="number" step="0.01" min="0.01" max="100" value={form.discountPercentage}
                  onChange={e => setForm(p => ({ ...p, discountPercentage: e.target.value }))}
                  className="input-field" placeholder="e.g. 20" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Max Discount ($)</label>
                <input type="number" step="0.01" value={form.maximumDiscountAmount}
                  onChange={e => setForm(p => ({ ...p, maximumDiscountAmount: e.target.value }))}
                  className="input-field" placeholder="Optional cap" />
              </div>
            </div>
          )}

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Min Order ($)</label>
              <input type="number" step="0.01" value={form.minimumOrderAmount}
                onChange={e => setForm(p => ({ ...p, minimumOrderAmount: e.target.value }))}
                className="input-field" placeholder="Optional" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Expiry Date *</label>
              <input type="datetime-local" value={form.expiryDate}
                onChange={e => setForm(p => ({ ...p, expiryDate: e.target.value }))}
                className="input-field" />
            </div>
          </div>
        </form>
      </Modal>
    </div>
  )
}
