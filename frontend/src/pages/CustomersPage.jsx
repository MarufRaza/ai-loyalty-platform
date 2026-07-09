import { useState, useEffect, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { Plus, Search, Filter, Users, Trash2, Eye, Loader2 } from 'lucide-react'
import customerService from '../services/customerService'
import Badge from '../components/UI/Badge'
import Pagination from '../components/UI/Pagination'
import LoadingSpinner from '../components/UI/LoadingSpinner'
import Modal from '../components/UI/Modal'
import toast from 'react-hot-toast'

const TIERS = ['', 'SILVER', 'GOLD', 'PLATINUM']

const EMPTY_FORM = { name: '', email: '', phone: '', city: '', lifetimeValue: '', lastPurchaseDate: '', notes: '' }

export default function CustomersPage() {
  const [customers, setCustomers] = useState([])
  const [loading, setLoading] = useState(true)
  const [pagination, setPagination] = useState({ page: 0, size: 20, totalPages: 0, totalElements: 0 })
  const [filters, setFilters] = useState({ search: '', city: '', tier: '' })
  const [showFilters, setShowFilters] = useState(false)
  const [showAddModal, setShowAddModal] = useState(false)
  const [addForm, setAddForm] = useState(EMPTY_FORM)
  const [addSubmitting, setAddSubmitting] = useState(false)
  const [formError, setFormError] = useState(null)

  const fetchCustomers = useCallback(async (page = 0) => {
    setLoading(true)
    try {
      const params = {
        page, size: pagination.size,
        ...(filters.search && { search: filters.search }),
        ...(filters.city && { city: filters.city }),
        ...(filters.tier && { tier: filters.tier }),
      }
      const data = await customerService.getAll(params)
      setCustomers(data.content || [])
      setPagination(p => ({
        ...p, page: data.number, totalPages: data.totalPages,
        totalElements: data.totalElements
      }))
    } catch (err) {
      toast.error('Failed to load customers')
    } finally {
      setLoading(false)
    }
  }, [filters, pagination.size])

  useEffect(() => { fetchCustomers(0) }, [])

  const handleSearch = (e) => {
    e.preventDefault()
    fetchCustomers(0)
  }

  const handleDelete = async (id, name) => {
    if (!confirm(`Delete customer "${name}"? This cannot be undone.`)) return
    try {
      await customerService.delete(id)
      toast.success('Customer deleted')
      fetchCustomers(pagination.page)
    } catch {
      toast.error('Delete failed')
    }
  }

  const handleCreate = async (e) => {
    e.preventDefault()
    setFormError(null)
    if (!addForm.name.trim() || !addForm.email.trim()) {
      setFormError('Name and email are required')
      return
    }
    setAddSubmitting(true)
    try {
      await customerService.create({
        name: addForm.name.trim(),
        email: addForm.email.trim(),
        phone: addForm.phone || null,
        city: addForm.city || null,
        lifetimeValue: addForm.lifetimeValue ? parseFloat(addForm.lifetimeValue) : null,
        lastPurchaseDate: addForm.lastPurchaseDate || null,
        notes: addForm.notes || null,
      })
      toast.success('Customer created!')
      setShowAddModal(false)
      setAddForm(EMPTY_FORM)
      setFormError(null)
      fetchCustomers(0)
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to create customer. Check all fields and try again.'
      setFormError(msg)
    } finally {
      setAddSubmitting(false)
    }
  }

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Customers</h1>
          <p className="text-gray-500 text-sm mt-0.5">
            {pagination.totalElements.toLocaleString()} total customers
          </p>
        </div>
        <button onClick={() => setShowAddModal(true)} className="btn-primary">
          <Plus className="w-4 h-4" />
          Add Customer
        </button>
      </div>

      {/* Search & Filters */}
      <div className="bg-white rounded-xl border border-gray-200 shadow-sm">
        <form onSubmit={handleSearch} className="flex items-center gap-3 p-4">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input
              type="text"
              placeholder="Search by name, email, city..."
              value={filters.search}
              onChange={e => setFilters(p => ({ ...p, search: e.target.value }))}
              className="input-field pl-10"
            />
          </div>
          <button type="button" onClick={() => setShowFilters(!showFilters)} className="btn-secondary">
            <Filter className="w-4 h-4" />
            Filters
          </button>
          <button type="submit" className="btn-primary">
            <Search className="w-4 h-4" />
            Search
          </button>
        </form>

        {showFilters && (
          <div className="px-4 pb-4 pt-0 flex gap-4 border-t border-gray-100">
            <div className="flex-1">
              <label className="text-xs font-medium text-gray-500 mb-1 block">City</label>
              <input
                type="text"
                placeholder="Filter by city"
                value={filters.city}
                onChange={e => setFilters(p => ({ ...p, city: e.target.value }))}
                className="input-field text-sm"
              />
            </div>
            <div className="flex-1">
              <label className="text-xs font-medium text-gray-500 mb-1 block">Loyalty Tier</label>
              <select
                value={filters.tier}
                onChange={e => setFilters(p => ({ ...p, tier: e.target.value }))}
                className="input-field text-sm"
              >
                {TIERS.map(t => (
                  <option key={t} value={t}>{t || 'All Tiers'}</option>
                ))}
              </select>
            </div>
            <div className="flex items-end">
              <button onClick={() => { setFilters({ search: '', city: '', tier: '' }); fetchCustomers(0) }}
                className="btn-secondary text-sm">
                Clear
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
        {loading ? (
          <LoadingSpinner text="Loading customers..." />
        ) : customers.length === 0 ? (
          <div className="py-16 text-center">
            <Users className="w-12 h-12 text-gray-300 mx-auto mb-3" />
            <p className="text-gray-500 font-medium">No customers found</p>
            <p className="text-gray-400 text-sm mt-1">Try adjusting your search or filters</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="table-header">Customer</th>
                  <th className="table-header">City</th>
                  <th className="table-header">Tier</th>
                  <th className="table-header">Points</th>
                  <th className="table-header">Lifetime Value</th>
                  <th className="table-header">Last Purchase</th>
                  <th className="table-header">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {customers.map((c) => (
                  <tr key={c.id} className="hover:bg-gray-50 transition-colors">
                    <td className="table-cell">
                      <div className="flex items-center gap-3">
                        <div className="w-9 h-9 rounded-full bg-gradient-to-br from-primary-500 to-indigo-500 flex items-center justify-center text-sm font-bold text-white flex-shrink-0">
                          {c.name.charAt(0).toUpperCase()}
                        </div>
                        <div>
                          <p className="font-medium text-gray-900">{c.name}</p>
                          <p className="text-xs text-gray-500">{c.email}</p>
                        </div>
                      </div>
                    </td>
                    <td className="table-cell text-gray-500">{c.city || '—'}</td>
                    <td className="table-cell">
                      <Badge tier={c.loyaltyTier} label={c.loyaltyTierDisplayName} />
                    </td>
                    <td className="table-cell font-medium text-gray-900">
                      {c.loyaltyPoints?.toLocaleString()}
                    </td>
                    <td className="table-cell font-medium text-green-700">
                      ${c.lifetimeValue?.toLocaleString()}
                    </td>
                    <td className="table-cell text-gray-500">
                      {c.lastPurchaseDate || '—'}
                    </td>
                    <td className="table-cell">
                      <div className="flex items-center gap-1">
                        <Link to={`/customers/${c.id}`}
                          className="p-1.5 rounded-lg text-gray-400 hover:text-primary-600 hover:bg-primary-50 transition-colors">
                          <Eye className="w-4 h-4" />
                        </Link>
                        <button
                          onClick={() => handleDelete(c.id, c.name)}
                          className="p-1.5 rounded-lg text-gray-400 hover:text-red-600 hover:bg-red-50 transition-colors">
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {!loading && customers.length > 0 && (
          <div className="border-t border-gray-100">
            <Pagination
              page={pagination.page}
              totalPages={pagination.totalPages}
              onPageChange={(p) => fetchCustomers(p)}
            />
          </div>
        )}
      </div>
      <Modal
        isOpen={showAddModal}
        onClose={() => { setShowAddModal(false); setAddForm(EMPTY_FORM); setFormError(null) }}
        title="Add New Customer"
        size="lg"
        footer={
          <>
            <button onClick={() => { setShowAddModal(false); setAddForm(EMPTY_FORM); setFormError(null) }} className="btn-secondary">Cancel</button>
            <button onClick={handleCreate} disabled={addSubmitting} className="btn-primary">
              {addSubmitting ? <Loader2 className="w-4 h-4 animate-spin" /> : <Plus className="w-4 h-4" />}
              Create Customer
            </button>
          </>
        }
      >
        <form onSubmit={handleCreate} className="space-y-4">
          {formError && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
              {formError}
            </div>
          )}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Name *</label>
              <input type="text" value={addForm.name}
                onChange={e => setAddForm(p => ({ ...p, name: e.target.value }))}
                className="input-field" placeholder="e.g. John Doe" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Email *</label>
              <input type="email" value={addForm.email}
                onChange={e => setAddForm(p => ({ ...p, email: e.target.value }))}
                className="input-field" placeholder="john@example.com" />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Phone</label>
              <input type="text" value={addForm.phone}
                onChange={e => setAddForm(p => ({ ...p, phone: e.target.value }))}
                className="input-field" placeholder="+923001234567" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">City</label>
              <input type="text" value={addForm.city}
                onChange={e => setAddForm(p => ({ ...p, city: e.target.value }))}
                className="input-field" placeholder="e.g. Karachi" />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Lifetime Value ($)</label>
              <input type="number" min="0" step="0.01" value={addForm.lifetimeValue}
                onChange={e => setAddForm(p => ({ ...p, lifetimeValue: e.target.value }))}
                className="input-field" placeholder="0.00" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Last Purchase Date</label>
              <input type="date" value={addForm.lastPurchaseDate}
                onChange={e => setAddForm(p => ({ ...p, lastPurchaseDate: e.target.value }))}
                className="input-field" />
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Notes</label>
            <textarea value={addForm.notes}
              onChange={e => setAddForm(p => ({ ...p, notes: e.target.value }))}
              className="input-field" rows={2} placeholder="Optional notes about this customer" />
          </div>
        </form>
      </Modal>
    </div>
  )
}
